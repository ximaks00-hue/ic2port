package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyEmitter;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.util.EnergyTransferHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * MV advanced solar panel — generates 8 EU/t during clear daylight.
 */
public class AdvancedSolarPanelBlockEntity extends BlockEntity implements IEnergyEmitter {

    public static final double BUFFER_CAPACITY = 800.0D;
    public static final double GENERATION_PER_TICK = 8.0D;
    public static final double MAX_OUTPUT_PER_TICK = 8.0D;
    public static final int TIER = EnergyTier.MV;

    private final LazyOptional<IEnergyEmitter> energyOptional = LazyOptional.of(() -> this);
    private double storedEnergy;

    public AdvancedSolarPanelBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.ADVANCED_SOLAR_PANEL_BE.get(), pos, state);
    }

    public static void serverTick(final Level level, final BlockPos pos, final BlockState state,
                                   final AdvancedSolarPanelBlockEntity panel) {
        panel.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide) return;
        double gen = calculateGeneration();
        if (gen > 0.0D) {
            double space = BUFFER_CAPACITY - storedEnergy;
            if (space > 0.0D) {
                storedEnergy += Math.min(gen, space);
                setChanged();
            }
        }
        distributeEnergy();
    }

    private double calculateGeneration() {
        if (level == null || !level.canSeeSky(worldPosition.above())) return 0.0D;
        if (!level.isDay() || level.isRaining() || level.isThundering()) return 0.0D;
        return GENERATION_PER_TICK;
    }

    private void distributeEnergy() {
        if (storedEnergy <= 0.0D) return;
        double toSend = Math.min(storedEnergy, MAX_OUTPUT_PER_TICK);
        double remainder = EnergyTransferHelper.injectIntoNeighbor(level, worldPosition, Direction.DOWN, toSend, TIER);
        double transferred = toSend - remainder;
        if (transferred > 0.0D) {
            storedEnergy -= transferred;
            setChanged();
        }
    }

    @Override
    public double getCapacity() { return BUFFER_CAPACITY; }
    @Override
    public double getStoredEnergy() { return storedEnergy; }
    @Override
    public int getTier() { return TIER; }
    @Override
    public double getOfferedEnergy() { return Math.min(storedEnergy, MAX_OUTPUT_PER_TICK); }
    @Override
    public void drawEnergy(final double amount) {
        storedEnergy = Math.max(0.0D, storedEnergy - amount);
        setChanged();
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("StoredEnergy", storedEnergy);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        storedEnergy = Math.min(tag.getDouble("StoredEnergy"), BUFFER_CAPACITY);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(final @NotNull Capability<T> capability,
                                                       final @Nullable Direction side) {
        if (capability == ModCapabilities.ENERGY_NODE_CAPABILITY && (side == null || side == Direction.DOWN)) {
            return energyOptional.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
    }
}
