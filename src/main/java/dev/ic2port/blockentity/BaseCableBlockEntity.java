package dev.ic2port.blockentity;

import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.api.energy.IEnergyConductor;
import dev.ic2port.energy.WorldEnergyNet;
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
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Shared buffering, forwarding, burnout and lazy-tick logic for insulated EU cables.
 */
public abstract class BaseCableBlockEntity extends BlockEntity implements IEnergyConductor, IEnergyAcceptor {

    private static final int INJECT_DEBOUNCE_TICKS = 2;
    private static final int NEIGHBOR_CACHE_INVALID = -1;

    private final double bufferCapacity;
    private final double transferLoss;
    private final int energyTier;
    private final boolean canBurnOut;

    private final LazyOptional<BaseCableBlockEntity> energyOptional = LazyOptional.of(() -> this);

    private double storedEnergy;
    private boolean burnedOut;
    @Nullable
    private Direction inputDirection;
    private int recentInjectTicks;
    private int neighborAcceptorMask = NEIGHBOR_CACHE_INVALID;

    protected BaseCableBlockEntity(
            final BlockEntityType<?> type,
            final BlockPos pos,
            final BlockState state,
            final double bufferCapacity,
            final double transferLoss,
            final int energyTier,
            final boolean canBurnOut) {
        super(type, pos, state);
        this.bufferCapacity = bufferCapacity;
        this.transferLoss = transferLoss;
        this.energyTier = energyTier;
        this.canBurnOut = canBurnOut;
    }

    public double getTransferLoss() {
        return transferLoss;
    }

    protected int getEnergyTier() {
        return energyTier;
    }

    protected void tickInjectDebounce() {
        if (recentInjectTicks > 0) {
            recentInjectTicks--;
        }
    }

    protected boolean hasRecentInject() {
        return recentInjectTicks > 0;
    }

    public void tickCable() {
        if (level == null || level.isClientSide || burnedOut) {
            return;
        }

        tickInjectDebounce();

        if (storedEnergy <= transferLoss && !hasRecentInject()) {
            storedEnergy = 0.0D;
            inputDirection = null;
            return;
        }

        if (storedEnergy <= transferLoss) {
            inputDirection = null;
            return;
        }

        double before = storedEnergy;
        storedEnergy = forwardEnergyPacket();
        onEnergyForwarded(before, storedEnergy);
        if (storedEnergy > 0.0D) {
            setChanged();
        }
        inputDirection = null;
    }

    /**
     * @deprecated Per-block ticker path; prefer {@link #tickCable()} via {@link WorldEnergyNet}.
     */
    @Deprecated
    protected void tickServer() {
        tickCable();
    }

    public boolean hasNetActivity() {
        return storedEnergy > transferLoss || hasRecentInject();
    }

    public boolean isBurnedOutForNet() {
        return burnedOut;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel && WorldEnergyNet.isEnabled()) {
            WorldEnergyNet.get(serverLevel).registerCable(this);
        }
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel serverLevel) {
            WorldEnergyNet.get(serverLevel).unregisterCable(worldPosition);
        }
        super.setRemoved();
    }

    protected double forwardEnergyPacket() {
        return EnergyTransferHelper.forwardCablePacket(
                level,
                worldPosition,
                inputDirection,
                storedEnergy,
                transferLoss,
                energyTier,
                getNeighborAcceptorMask());
    }

    protected void onEnergyForwarded(final double energyBefore, final double energyAfter) {
    }

    protected void onEnergyAccepted(final double accepted) {
    }

    protected void markRecentInject() {
        recentInjectTicks = INJECT_DEBOUNCE_TICKS;
    }

    @Nullable
    protected Direction getInputDirection() {
        return inputDirection;
    }

    protected void setInputDirection(@Nullable final Direction direction) {
        inputDirection = direction;
    }

    protected double getBufferedEnergy() {
        return storedEnergy;
    }

    protected void setBufferedEnergy(final double energy) {
        storedEnergy = energy;
    }

    protected boolean isBurnedOut() {
        return burnedOut;
    }

    public void invalidateNeighborAcceptorCache() {
        neighborAcceptorMask = NEIGHBOR_CACHE_INVALID;
    }

    public static void notifyNeighborUpdate(
            final LevelAccessor level,
            final BlockPos cablePos,
            final BlockPos neighborPos) {
        BlockEntity cableEntity = level.getBlockEntity(cablePos);
        if (cableEntity instanceof BaseCableBlockEntity cable) {
            cable.invalidateNeighborAcceptorCache();
        }
        BlockEntity neighborEntity = level.getBlockEntity(neighborPos);
        if (neighborEntity instanceof BaseCableBlockEntity neighborCable) {
            neighborCable.invalidateNeighborAcceptorCache();
        }
        if (level instanceof ServerLevel serverLevel && WorldEnergyNet.isEnabled()) {
            WorldEnergyNet.get(serverLevel).invalidateGrid(cablePos);
            if (!cablePos.equals(neighborPos)) {
                WorldEnergyNet.get(serverLevel).invalidateGrid(neighborPos);
            }
        }
    }

    private int getNeighborAcceptorMask() {
        if (neighborAcceptorMask != NEIGHBOR_CACHE_INVALID) {
            return neighborAcceptorMask;
        }
        if (level == null) {
            return 0;
        }
        if (level instanceof ServerLevel serverLevel && WorldEnergyNet.isEnabled()) {
            neighborAcceptorMask = WorldEnergyNet.get(serverLevel).getAcceptorMaskForCable(worldPosition);
            return neighborAcceptorMask;
        }
        neighborAcceptorMask = EnergyTransferHelper.buildNeighborAcceptorMask(level, worldPosition);
        return neighborAcceptorMask;
    }

    @Override
    public double injectEnergy(final Direction directionFrom, final double amount, final int tier) {
        if (level == null || level.isClientSide || amount <= 0.0D || burnedOut) {
            return amount;
        }
        if (tier > getTier()) {
            if (canBurnOut
                    && ModConfig.CABLE_BURNOUT_ENABLED.get()
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
        markRecentInject();
        onEnergyAccepted(accepted);
        if (level instanceof ServerLevel serverLevel && WorldEnergyNet.isEnabled()) {
            WorldEnergyNet.get(serverLevel).markActive(worldPosition);
        }
        setChanged();
        return amount - accepted;
    }

    @Override
    public double getCapacity() {
        return bufferCapacity;
    }

    @Override
    public double getStoredEnergy() {
        return storedEnergy;
    }

    @Override
    public int getTier() {
        return energyTier;
    }

    protected void burnOut() {
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
        storedEnergy = Math.min(tag.getDouble("StoredEnergy"), bufferCapacity);
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
