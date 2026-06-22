package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyEmitter;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.util.EnergyTransferHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * LV passive hydro generator — output scales with adjacent water blocks.
 */
public class WaterMillBlockEntity extends BlockEntity implements IEnergyEmitter {

    public static final double BUFFER_CAPACITY = 100.0D;
    public static final double EU_PER_WATER_BLOCK = 0.025D;
    public static final double MAX_GENERATION_PER_TICK = 0.75D;
    public static final int RECALC_INTERVAL_TICKS = 80;
    public static final int NEIGHBOR_POSITIONS = 26;
    public static final int TIER = EnergyTier.LV;

    private static final int SCAN_RADIUS = 1;

    private final LazyOptional<IEnergyEmitter> energyOptional = LazyOptional.of(() -> this);

    private double storedEnergy;
    private int tickCounter;
    private int cachedWaterBlocks;

    public WaterMillBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.WATER_MILL_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final WaterMillBlockEntity waterMill) {
        waterMill.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide) {
            return;
        }

        if (tickCounter++ % RECALC_INTERVAL_TICKS == 0) {
            refreshWaterCache();
        }

        double generation = getCurrentGeneration();
        if (generation > 0.0D) {
            double space = BUFFER_CAPACITY - storedEnergy;
            if (space > 0.0D) {
                storedEnergy += Math.min(generation, space);
                setChanged();
            }
        }

        distributeEnergy();
    }

    private void refreshWaterCache() {
        if (level == null) {
            cachedWaterBlocks = 0;
            return;
        }

        int waterBlocks = 0;
        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dy = -SCAN_RADIUS; dy <= SCAN_RADIUS; dy++) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    BlockPos checkPos = worldPosition.offset(dx, dy, dz);
                    if (isWater(level.getFluidState(checkPos))) {
                        waterBlocks++;
                    }
                }
            }
        }
        cachedWaterBlocks = waterBlocks;
    }

    private static boolean isWater(final FluidState fluidState) {
        return fluidState.is(Fluids.WATER) || fluidState.is(Fluids.FLOWING_WATER);
    }

    private double getCurrentGeneration() {
        return Math.min(MAX_GENERATION_PER_TICK, cachedWaterBlocks * EU_PER_WATER_BLOCK);
    }

    private void distributeEnergy() {
        if (storedEnergy <= 0.0D || level == null) {
            return;
        }

        double remaining = Math.min(storedEnergy, EnergyTier.LV_MAX_PACKET);
        for (Direction direction : Direction.values()) {
            if (remaining <= 0.0D) {
                break;
            }
            double notAccepted = EnergyTransferHelper.injectIntoNeighbor(
                    level, worldPosition, direction, remaining, TIER);
            double transferred = remaining - notAccepted;
            if (transferred > 0.0D) {
                storedEnergy -= transferred;
                remaining = notAccepted;
                setChanged();
            }
        }
    }

    public Component getDebugMessage() {
        double generation = getCurrentGeneration();
        return Component.translatable(
                "message.ic2port.water_mill.debug",
                (int) Math.round(storedEnergy),
                cachedWaterBlocks,
                String.format("%.2f", generation)).withStyle(ChatFormatting.GRAY);
    }

    @Override
    public double getCapacity() {
        return BUFFER_CAPACITY;
    }

    @Override
    public double getStoredEnergy() {
        return storedEnergy;
    }

    @Override
    public int getTier() {
        return TIER;
    }

    @Override
    public double getOfferedEnergy() {
        return Math.min(storedEnergy, EnergyTier.LV_MAX_PACKET);
    }

    @Override
    public void drawEnergy(final double amount) {
        if (amount <= 0.0D) {
            return;
        }
        storedEnergy = Math.max(0.0D, storedEnergy - amount);
        setChanged();
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("StoredEnergy", storedEnergy);
        tag.putInt("TickCounter", tickCounter);
        tag.putInt("CachedWaterBlocks", cachedWaterBlocks);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        storedEnergy = Math.min(tag.getDouble("StoredEnergy"), BUFFER_CAPACITY);
        tickCounter = tag.getInt("TickCounter");
        cachedWaterBlocks = tag.getInt("CachedWaterBlocks");
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            final @NotNull Capability<T> capability,
            final @Nullable Direction side) {
        if (capability == ModCapabilities.ENERGY_NODE_CAPABILITY) {
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
