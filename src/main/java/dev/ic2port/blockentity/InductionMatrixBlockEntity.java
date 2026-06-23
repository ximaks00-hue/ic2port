package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.api.energy.IEnergyEmitter;
import dev.ic2port.api.energy.IEnergyNode;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.util.EnergyTransferHelper;
import dev.ic2port.util.InductionMatrixMultiblock;
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
 * MVP induction matrix controller — stores EU when the 5×5×5 shell is valid.
 */
public class InductionMatrixBlockEntity extends BlockEntity implements IEnergyAcceptor, IEnergyEmitter {

    public static final double BASE_CAPACITY = 1_000_000.0D;
    public static final double CAPACITY_PER_CASING = 250_000.0D;
    public static final double MAX_OUTPUT = EnergyTier.EV_MAX_PACKET;
    public static final int TIER = EnergyTier.EV;

    private final LazyOptional<IEnergyNode> energyOptional = LazyOptional.of(() -> this);

    private double storedEnergy;
    private boolean structureValid;

    public InductionMatrixBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.INDUCTION_MATRIX_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final InductionMatrixBlockEntity matrix) {
        matrix.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide) {
            return;
        }
        structureValid = InductionMatrixMultiblock.isValid(level, worldPosition);
        if (storedEnergy > 0.0D && structureValid) {
            emitEnergy();
        }
    }

    private void emitEnergy() {
        for (Direction direction : Direction.values()) {
            double offered = Math.min(storedEnergy, MAX_OUTPUT);
            double remainder = EnergyTransferHelper.injectIntoNeighbor(level, worldPosition, direction, offered, TIER);
            double transferred = offered - remainder;
            if (transferred > 0.0D) {
                storedEnergy -= transferred;
                setChanged();
                return;
            }
        }
    }

    @Override
    public double injectEnergy(final Direction directionFrom, final double amount, final int tier) {
        if (level == null || level.isClientSide || amount <= 0.0D || !structureValid) {
            return amount;
        }
        if (tier > TIER) {
            return amount;
        }
        double space = getEnergyCapacity() - storedEnergy;
        double accepted = Math.min(amount, space);
        storedEnergy += accepted;
        setChanged();
        return amount - accepted;
    }

    public double getEnergyCapacity() {
        if (!structureValid || level == null) {
            return BASE_CAPACITY;
        }
        int casings = InductionMatrixMultiblock.countCasingBlocks(level, worldPosition);
        return BASE_CAPACITY + casings * CAPACITY_PER_CASING;
    }

    @Override
    public double getCapacity() {
        return getEnergyCapacity();
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
        return structureValid ? Math.min(storedEnergy, MAX_OUTPUT) : 0.0D;
    }

    @Override
    public void drawEnergy(final double amount) {
        storedEnergy = Math.max(0.0D, storedEnergy - amount);
        setChanged();
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("StoredEnergy", storedEnergy);
        tag.putBoolean("StructureValid", structureValid);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        storedEnergy = tag.getDouble("StoredEnergy");
        structureValid = tag.getBoolean("StructureValid");
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
