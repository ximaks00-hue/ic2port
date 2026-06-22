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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Directional LV cable — input from back, output to facing side only. */
public class SplitterCableBlockEntity extends BlockEntity implements IEnergyConductor, IEnergyAcceptor {

    public static final double BUFFER_CAPACITY = 32.0D;
    public static final double TRANSFER_LOSS = 0.2D;
    private static final int TIER = EnergyTier.LV;

    private final LazyOptional<SplitterCableBlockEntity> energyOptional = LazyOptional.of(() -> this);
    private double storedEnergy;
    private boolean burnedOut;

    public SplitterCableBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.SPLITTER_CABLE_BE.get(), pos, state);
    }

    private Direction getOutputFace() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    public static void serverTick(final Level level, final BlockPos pos, final BlockState state,
                                   final SplitterCableBlockEntity cable) {
        cable.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide || burnedOut) {
            return;
        }
        if (storedEnergy <= TRANSFER_LOSS) {
            storedEnergy = 0.0D;
            return;
        }
        Direction out = getOutputFace();
        double packet = storedEnergy;
        double remainder = EnergyTransferHelper.injectIntoNeighbor(level, worldPosition, out, packet, TIER);
        storedEnergy = Math.max(0.0D, remainder - TRANSFER_LOSS);
        setChanged();
    }

    @Override
    public double injectEnergy(final Direction directionFrom, final double amount, final int tier) {
        if (level == null || level.isClientSide || amount <= 0.0D || burnedOut) return amount;
        if (directionFrom != getOutputFace().getOpposite()) return amount;
        if (tier > getTier()) {
            if (ModConfig.CABLE_BURNOUT_ENABLED.get()
                    && level.random.nextDouble() <= ModConfig.CABLE_BURNOUT_CHANCE.get()) {
                burnOut();
            }
            return amount;
        }
        double space = BUFFER_CAPACITY - storedEnergy;
        double accepted = Math.min(amount, space);
        if (accepted <= 0.0D) return amount;
        storedEnergy += accepted;
        setChanged();
        return amount - accepted;
    }

    private void burnOut() {
        if (level == null || burnedOut) return;
        burnedOut = true;
        storedEnergy = 0.0D;
        level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.7F, 1.0F);
        if (level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SMOKE, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                    worldPosition.getZ() + 0.5, 4, 0.1, 0.1, 0.1, 0.01);
        }
        level.setBlock(worldPosition, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    @Override public double getCapacity() { return BUFFER_CAPACITY; }
    @Override public double getStoredEnergy() { return storedEnergy; }
    @Override public int getTier() { return TIER; }

    @Override protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("StoredEnergy", storedEnergy);
    }

    @Override public void load(final CompoundTag tag) {
        super.load(tag);
        storedEnergy = Math.min(tag.getDouble("StoredEnergy"), BUFFER_CAPACITY);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(final @NotNull Capability<T> cap, final @Nullable Direction side) {
        if (cap != ModCapabilities.ENERGY_NODE_CAPABILITY) return super.getCapability(cap, side);
        Direction out = getOutputFace();
        if (side == null || side == out || side == out.getOpposite()) return energyOptional.cast();
        return LazyOptional.empty();
    }

    @Override public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
    }
}
