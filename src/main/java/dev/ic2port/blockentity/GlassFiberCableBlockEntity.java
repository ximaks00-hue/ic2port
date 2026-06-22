package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.api.energy.IEnergyConductor;
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
 * Glass fibre cable — EV-tier conductor that never burns out from overload.
 */
public class GlassFiberCableBlockEntity extends BlockEntity implements IEnergyConductor, IEnergyAcceptor {

    public static final double BUFFER_CAPACITY = 2048.0D;
    public static final double TRANSFER_LOSS = 0.2D;
    private static final int TIER = EnergyTier.EV;

    private final LazyOptional<GlassFiberCableBlockEntity> energyOptional = LazyOptional.of(() -> this);

    private double storedEnergy;
    @Nullable
    private Direction inputDirection;

    public GlassFiberCableBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.GLASS_FIBER_CABLE_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final GlassFiberCableBlockEntity cable) {
        cable.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (storedEnergy <= TRANSFER_LOSS) {
            storedEnergy = 0.0D;
            inputDirection = null;
            return;
        }

        storedEnergy = EnergyTransferHelper.forwardCablePacket(
                level, worldPosition, inputDirection, storedEnergy, TRANSFER_LOSS, TIER);
        if (storedEnergy > 0.0D) {
            setChanged();
        }
        inputDirection = null;
    }

    @Override
    public double injectEnergy(final Direction directionFrom, final double amount, final int tier) {
        if (level == null || level.isClientSide || amount <= 0.0D) {
            return amount;
        }
        if (tier > getTier()) {
            return amount;
        }

        double space = getCapacity() - storedEnergy;
        double accepted = Math.min(amount, space);
        if (accepted <= 0.0D) {
            return amount;
        }

        inputDirection = directionFrom;
        storedEnergy += accepted;
        setChanged();
        return amount - accepted;
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
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("StoredEnergy", storedEnergy);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        storedEnergy = Math.min(tag.getDouble("StoredEnergy"), BUFFER_CAPACITY);
    }

    public ComponentView getDebugStatus() {
        return new ComponentView(storedEnergy, inputDirection);
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

    public record ComponentView(double storedEnergy, @Nullable Direction inputDirection) {
    }
}
