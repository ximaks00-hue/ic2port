package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.api.energy.IEnergyNode;
import dev.ic2port.block.LVTransformerBlock;
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
 * LV Transformer — steps MV down to LV (default) or LV up to MV (redstone powered).
 */
public class LVTransformerBlockEntity extends BlockEntity implements IEnergyAcceptor {

    public static final double BUFFER_CAPACITY = 128.0D;
    public static final double LV_OUTPUT_PER_SIDE = 32.0D;
    public static final double MV_PACKET = 128.0D;

    private final LazyOptional<IEnergyNode> energyOptional = LazyOptional.of(() -> this);

    private double storedEnergy;
    private boolean destroyedByOverload;

    public LVTransformerBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.LV_TRANSFORMER_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final LVTransformerBlockEntity transformer) {
        transformer.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide || destroyedByOverload) {
            return;
        }

        if (isStepUpMode()) {
            tryStepUpOutput();
        } else if (storedEnergy > 0.0D) {
            distributeLvOutputs();
        }
    }

    private boolean isStepUpMode() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }

    private Direction getHighVoltageFace() {
        return getBlockState().getValue(LVTransformerBlock.FACING);
    }

    @Override
    public double injectEnergy(final Direction directionFrom, final double amount, final int tier) {
        if (level == null || level.isClientSide || amount <= 0.0D || destroyedByOverload) {
            return amount;
        }

        Direction highVoltageFace = getHighVoltageFace();
        boolean stepUp = isStepUpMode();

        if (stepUp) {
            if (directionFrom == highVoltageFace) {
                return amount;
            }
            if (tier > EnergyTier.LV) {
                explode(tier);
                return amount;
            }
            if (tier < EnergyTier.LV) {
                return amount;
            }

            double accepted = acceptEnergy(amount);
            if (storedEnergy >= MV_PACKET) {
                tryStepUpOutput();
            }
            return amount - accepted;
        }

        if (directionFrom != highVoltageFace) {
            return amount;
        }
        if (tier > EnergyTier.MV) {
            explode(tier);
            return amount;
        }
        if (tier < EnergyTier.MV) {
            return amount;
        }

        double accepted = acceptEnergy(amount);
        distributeLvOutputs();
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

    private void distributeLvOutputs() {
        if (level == null || storedEnergy <= 0.0D) {
            return;
        }

        Direction highVoltageFace = getHighVoltageFace();
        for (Direction direction : Direction.values()) {
            if (direction == highVoltageFace || storedEnergy <= 0.0D) {
                continue;
            }

            double toSend = Math.min(LV_OUTPUT_PER_SIDE, storedEnergy);
            double remainder = EnergyTransferHelper.injectIntoNeighbor(
                    level, worldPosition, direction, toSend, EnergyTier.LV);
            double transferred = toSend - remainder;
            if (transferred > 0.0D) {
                storedEnergy -= transferred;
                setChanged();
            }
        }
    }

    private void tryStepUpOutput() {
        if (level == null || storedEnergy < MV_PACKET) {
            return;
        }

        Direction highVoltageFace = getHighVoltageFace();
        double remainder = EnergyTransferHelper.injectIntoNeighbor(
                level, worldPosition, highVoltageFace, MV_PACKET, EnergyTier.MV);
        double transferred = MV_PACKET - remainder;
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
                + (incomingTier - EnergyTier.MV) * 1.5F;
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
        return isStepUpMode() ? EnergyTier.LV : EnergyTier.MV;
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
