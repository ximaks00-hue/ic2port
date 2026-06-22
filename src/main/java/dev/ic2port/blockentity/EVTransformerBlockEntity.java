package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.api.energy.IEnergyNode;
import dev.ic2port.block.EVTransformerBlock;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.setup.ModConfig;
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
 * EV Transformer — steps EV down to HV (default) or HV up to EV (redstone powered).
 */
public class EVTransformerBlockEntity extends BlockEntity implements IEnergyAcceptor {

    public static final double BUFFER_CAPACITY = 2048.0D;
    public static final double HV_OUTPUT_PER_SIDE = 512.0D;
    public static final double EV_PACKET = 2048.0D;

    private final LazyOptional<IEnergyNode> energyOptional = LazyOptional.of(() -> this);

    private double storedEnergy;
    private boolean destroyedByOverload;

    public EVTransformerBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.EV_TRANSFORMER_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final EVTransformerBlockEntity transformer) {
        transformer.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide || destroyedByOverload) {
            return;
        }

        if (isStepUpMode()) {
            tryStepUpOutput();
        } else if (storedEnergy > 0.0D) {
            distributeHvOutputs();
        }
    }

    private boolean isStepUpMode() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }

    private Direction getExtremeVoltageFace() {
        return getBlockState().getValue(EVTransformerBlock.FACING);
    }

    @Override
    public double injectEnergy(final Direction directionFrom, final double amount, final int tier) {
        if (level == null || level.isClientSide || amount <= 0.0D || destroyedByOverload) {
            return amount;
        }

        Direction extremeVoltageFace = getExtremeVoltageFace();
        boolean stepUp = isStepUpMode();

        if (stepUp) {
            if (directionFrom == extremeVoltageFace) {
                return amount;
            }
            if (tier > EnergyTier.HV) {
                explode(tier);
                return amount;
            }
            if (tier < EnergyTier.HV) {
                return amount;
            }

            double accepted = acceptEnergy(amount);
            if (storedEnergy >= EV_PACKET) {
                tryStepUpOutput();
            }
            return amount - accepted;
        }

        if (directionFrom != extremeVoltageFace) {
            return amount;
        }
        if (tier > EnergyTier.EV) {
            explode(tier);
            return amount;
        }
        if (tier < EnergyTier.EV) {
            return amount;
        }

        double accepted = acceptEnergy(amount);
        distributeHvOutputs();
        return amount - accepted;
    }

    private double acceptEnergy(final double amount) {
        double space = BUFFER_CAPACITY - storedEnergy;
        double accepted = Math.min(amount, space);
        if (accepted <= 0.0D) {
            return 0.0D;
        }
        storedEnergy += accepted;
        setChanged();
        return accepted;
    }

    private void distributeHvOutputs() {
        if (level == null || storedEnergy <= 0.0D) {
            return;
        }

        Direction extremeVoltageFace = getExtremeVoltageFace();
        for (Direction direction : Direction.values()) {
            if (direction == extremeVoltageFace || storedEnergy <= 0.0D) {
                continue;
            }

            double toSend = Math.min(HV_OUTPUT_PER_SIDE, storedEnergy);
            double remainder = EnergyTransferHelper.injectIntoNeighbor(
                    level, worldPosition, direction, toSend, EnergyTier.HV);
            double transferred = toSend - remainder;
            if (transferred > 0.0D) {
                storedEnergy -= transferred;
                setChanged();
            }
        }
    }

    private void tryStepUpOutput() {
        if (level == null || storedEnergy < EV_PACKET) {
            return;
        }

        Direction extremeVoltageFace = getExtremeVoltageFace();
        double remainder = EnergyTransferHelper.injectIntoNeighbor(
                level, worldPosition, extremeVoltageFace, EV_PACKET, EnergyTier.EV);
        double transferred = EV_PACKET - remainder;
        if (transferred > 0.0D) {
            storedEnergy -= transferred;
            setChanged();
        }
    }

    private void explode(final int incomingTier) {
        if (level == null || level.isClientSide || destroyedByOverload) {
            return;
        }
        destroyedByOverload = true;
        storedEnergy = 0.0D;

        float radius = ModConfig.EXPLOSION_BASE_RADIUS.get().floatValue()
                + Math.max(0, incomingTier - EnergyTier.EV) * 1.5F;
        double centerX = worldPosition.getX() + 0.5D;
        double centerY = worldPosition.getY() + 0.5D;
        double centerZ = worldPosition.getZ() + 0.5D;

        level.removeBlock(worldPosition, false);
        level.explode(null, centerX, centerY, centerZ, radius, Level.ExplosionInteraction.TNT);
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
        return isStepUpMode() ? EnergyTier.HV : EnergyTier.EV;
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
