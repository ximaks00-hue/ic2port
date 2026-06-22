package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyEmitter;
import dev.ic2port.block.WindMillBlock;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.util.EnergyTransferHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * LV passive wind generator — output scales with height and clear space around the rotor.
 */
public class WindMillBlockEntity extends BlockEntity implements IEnergyEmitter {

    public static final double BUFFER_CAPACITY = 100.0D;
    public static final double MAX_HEIGHT_BASE = 2.0D;
    public static final int SEA_LEVEL = 64;
    public static final int RECALC_INTERVAL_TICKS = 100;
    public static final double OBSTACLE_PENALTY = 0.10D;
    public static final double STORM_MULTIPLIER = 1.5D;
    public static final int TIER = EnergyTier.LV;

    private static final int SCAN_RADIUS = 2;

    private final LazyOptional<IEnergyEmitter> energyOptional = LazyOptional.of(() -> this);

    private double storedEnergy;
    private int tickCounter;
    private double cachedHeightBase;
    private double cachedObstacleEfficiency;

    public WindMillBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.WIND_MILL_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final WindMillBlockEntity windMill) {
        windMill.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide) {
            return;
        }

        if (tickCounter++ % RECALC_INTERVAL_TICKS == 0) {
            refreshGenerationCache();
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

    private void refreshGenerationCache() {
        if (level == null) {
            cachedHeightBase = 0.0D;
            cachedObstacleEfficiency = 0.0D;
            return;
        }

        int y = worldPosition.getY();
        if (y < SEA_LEVEL) {
            cachedHeightBase = 0.0D;
        } else {
            cachedHeightBase = Math.min(MAX_HEIGHT_BASE, (y - SEA_LEVEL) / 64.0D);
        }

        int obstructingBlocks = countObstructingBlocks();
        cachedObstacleEfficiency = Math.max(0.0D, 1.0D - obstructingBlocks * OBSTACLE_PENALTY);
    }

    private int countObstructingBlocks() {
        if (level == null) {
            return 0;
        }

        int count = 0;
        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dy = -SCAN_RADIUS; dy <= SCAN_RADIUS; dy++) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    BlockPos checkPos = worldPosition.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(checkPos);
                    if (isObstructing(state)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static boolean isObstructing(final BlockState state) {
        return !state.isAir() && !state.is(BlockTags.LEAVES);
    }

    private double getCurrentGeneration() {
        double generation = cachedHeightBase * cachedObstacleEfficiency;
        if (level != null && (level.isRaining() || level.isThundering())) {
            generation *= STORM_MULTIPLIER;
        }
        return generation;
    }

    private Direction getOutputFace() {
        return getBlockState().getValue(WindMillBlock.FACING).getOpposite();
    }

    private void distributeEnergy() {
        if (storedEnergy <= 0.0D || level == null) {
            return;
        }

        double toSend = Math.min(storedEnergy, EnergyTier.LV_MAX_PACKET);

        double remainder = EnergyTransferHelper.injectIntoNeighbor(
                level, worldPosition, getOutputFace(), toSend, TIER);
        double transferred = toSend - remainder;
        if (transferred > 0.0D) {
            storedEnergy -= transferred;
            setChanged();
        }
    }

    public Component getDebugMessage() {
        double generation = getCurrentGeneration();
        int efficiencyPercent = (int) Math.round(cachedObstacleEfficiency * 100.0D);
        return Component.translatable(
                "message.ic2port.wind_mill.debug",
                (int) Math.round(storedEnergy),
                String.format("%.2f", generation),
                efficiencyPercent).withStyle(ChatFormatting.GRAY);
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
        tag.putDouble("CachedHeightBase", cachedHeightBase);
        tag.putDouble("CachedObstacleEfficiency", cachedObstacleEfficiency);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        storedEnergy = Math.min(tag.getDouble("StoredEnergy"), BUFFER_CAPACITY);
        tickCounter = tag.getInt("TickCounter");
        cachedHeightBase = tag.getDouble("CachedHeightBase");
        cachedObstacleEfficiency = tag.getDouble("CachedObstacleEfficiency");
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            final @NotNull Capability<T> capability,
            final @Nullable Direction side) {
        if (capability == ModCapabilities.ENERGY_NODE_CAPABILITY) {
            Direction outputFace = getOutputFace();
            if (side == null || side == outputFace) {
                return energyOptional.cast();
            }
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
    }
}
