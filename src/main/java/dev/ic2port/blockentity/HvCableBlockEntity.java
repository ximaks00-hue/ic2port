package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.api.energy.IEnergyConductor;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.setup.ModConfig;
import dev.ic2port.util.EnergyTransferHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Insulated HV iron cable — buffers EU and forwards it to neighbors with a small loss.
 */
public class HvCableBlockEntity extends BlockEntity implements IEnergyConductor, IEnergyAcceptor {

    public static final double BUFFER_CAPACITY = 512.0D;
    public static final double TRANSFER_LOSS = 0.2D;
    private static final int TIER = EnergyTier.HV;

    private final LazyOptional<HvCableBlockEntity> energyOptional = LazyOptional.of(() -> this);

    private double storedEnergy;
    private boolean burnedOut;
    @Nullable
    private Direction inputDirection;

    public HvCableBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.HV_CABLE_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final HvCableBlockEntity cable) {
        cable.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide || burnedOut) {
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
        if (level == null || level.isClientSide || amount <= 0.0D || burnedOut) {
            return amount;
        }
        if (tier > getTier()) {
            if (ModConfig.CABLE_BURNOUT_ENABLED.get()
                    && level.random.nextDouble() <= ModConfig.CABLE_BURNOUT_CHANCE.get()) {
                burnOut();
            }
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

    private void burnOut() {
        if (level == null || level.isClientSide || burnedOut) {
            return;
        }
        burnedOut = true;
        storedEnergy = 0.0D;
        inputDirection = null;

        level.playSound(
                null,
                worldPosition,
                SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS,
                0.7F,
                1.0F);
        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 8; i++) {
                double offsetX = worldPosition.getX() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.6D;
                double offsetY = worldPosition.getY() + 0.5D + level.random.nextDouble() * 0.4D;
                double offsetZ = worldPosition.getZ() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.6D;
                serverLevel.sendParticles(ParticleTypes.SMOKE, offsetX, offsetY, offsetZ, 1, 0.0D, 0.05D, 0.0D, 0.01D);
            }
        }
        level.setBlock(worldPosition, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
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
