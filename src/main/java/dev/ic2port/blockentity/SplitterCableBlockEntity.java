package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.setup.ModConfig;
import dev.ic2port.util.EnergyTransferHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Directional LV cable — input from back, output to facing side only. */
public class SplitterCableBlockEntity extends BaseCableBlockEntity {

    public static final double BUFFER_CAPACITY = 32.0D;
    public static final double TRANSFER_LOSS = 0.2D;

    public SplitterCableBlockEntity(final BlockPos pos, final BlockState state) {
        super(
                BlockEntityRegistry.SPLITTER_CABLE_BE.get(),
                pos,
                state,
                BUFFER_CAPACITY,
                TRANSFER_LOSS,
                EnergyTier.LV,
                true);
    }

    private Direction getOutputFace() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final SplitterCableBlockEntity cable) {
        cable.tickServer();
    }

    @Override
    public void tickCable() {
        if (level == null || level.isClientSide || isBurnedOut()) {
            return;
        }

        tickInjectDebounce();

        if (getBufferedEnergy() <= TRANSFER_LOSS && !hasRecentInject()) {
            setBufferedEnergy(0.0D);
            return;
        }

        if (getBufferedEnergy() <= TRANSFER_LOSS) {
            return;
        }

        Direction out = getOutputFace();
        double packet = getBufferedEnergy() - TRANSFER_LOSS;
        double remainder = EnergyTransferHelper.injectIntoNeighbor(level, worldPosition, out, packet, getTier());
        setBufferedEnergy(Math.max(0.0D, remainder));
        setChanged();
    }

    @Override
    public double injectEnergy(final Direction directionFrom, final double amount, final int tier) {
        if (level == null || level.isClientSide || amount <= 0.0D || isBurnedOut()) {
            return amount;
        }
        if (directionFrom != getOutputFace().getOpposite()) {
            return amount;
        }
        if (tier > getTier()) {
            if (ModConfig.CABLE_BURNOUT_ENABLED.get()
                    && level.random.nextDouble() <= ModConfig.CABLE_BURNOUT_CHANCE.get()) {
                burnOutSplitter();
            }
            return amount;
        }
        double space = BUFFER_CAPACITY - getBufferedEnergy();
        double accepted = Math.min(amount, space);
        if (accepted <= 0.0D) {
            return amount;
        }
        setBufferedEnergy(getBufferedEnergy() + accepted);
        markRecentInject();
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel
                && dev.ic2port.energy.WorldEnergyNet.isEnabled()) {
            dev.ic2port.energy.WorldEnergyNet.get(serverLevel).markActive(worldPosition);
        }
        setChanged();
        return amount - accepted;
    }

    private void burnOutSplitter() {
        if (level == null || isBurnedOut()) {
            return;
        }
        setBufferedEnergy(0.0D);
        level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.7F, 1.0F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.SMOKE,
                    worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 0.5D,
                    worldPosition.getZ() + 0.5D,
                    4,
                    0.1D,
                    0.1D,
                    0.1D,
                    0.01D);
        }
        level.setBlock(worldPosition, Blocks.AIR.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            final @NotNull Capability<T> capability,
            final @Nullable Direction side) {
        if (capability != ModCapabilities.ENERGY_NODE_CAPABILITY) {
            return super.getCapability(capability, side);
        }
        Direction out = getOutputFace();
        if (side == null || side == out || side == out.getOpposite()) {
            return super.getCapability(capability, side);
        }
        return LazyOptional.empty();
    }
}
