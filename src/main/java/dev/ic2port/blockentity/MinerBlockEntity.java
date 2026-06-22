package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.api.energy.IEnergyNode;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ModCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * HV block miner — digs downward one block every 20 ticks, consuming 100 EU/block.
 * Drops items into its internal output buffer; pipe or hopper to collect.
 */
public class MinerBlockEntity extends BlockEntity implements IEnergyAcceptor {

    public static final double ENERGY_CAPACITY = 20_000.0D;
    public static final double EU_PER_BLOCK = 100.0D;
    public static final int TIER = EnergyTier.HV;
    public static final int OUTPUT_SLOTS = 9;
    private static final int MINE_INTERVAL_TICKS = 20;

    private final ItemStackHandler outputHandler = new ItemStackHandler(OUTPUT_SLOTS) {
        @Override
        protected void onContentsChanged(final int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> outputOptional = LazyOptional.of(() -> outputHandler);
    private final LazyOptional<IEnergyNode> energyOptional = LazyOptional.of(() -> this);

    private double storedEnergy;
    private int mineY;
    private int tickCount;
    private boolean done;

    public MinerBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.MINER_BE.get(), pos, state);
        this.mineY = pos.getY() - 1;
    }

    public static void serverTick(final Level level, final BlockPos pos, final BlockState state,
                                   final MinerBlockEntity miner) {
        miner.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide || done) return;
        if (storedEnergy < EU_PER_BLOCK) return;

        tickCount++;
        if (tickCount < MINE_INTERVAL_TICKS) return;
        tickCount = 0;

        BlockPos targetPos = new BlockPos(worldPosition.getX(), mineY, worldPosition.getZ());
        if (!level.isInWorldBounds(targetPos) || mineY < level.getMinBuildHeight()) {
            done = true;
            setChanged();
            return;
        }

        BlockState targetState = level.getBlockState(targetPos);
        if (targetState.isAir() || targetState.liquid()) {
            mineY--;
            setChanged();
            return;
        }

        float hardness = targetState.getDestroySpeed(level, targetPos);
        if (hardness < 0.0F) {
            mineY--;
            setChanged();
            return;
        }

        if (level instanceof ServerLevel serverLevel) {
            List<ItemStack> drops = Block.getDrops(
                    targetState, serverLevel, targetPos, level.getBlockEntity(targetPos));
            for (ItemStack drop : drops) {
                ItemStack remaining = drop.copy();
                for (int i = 0; i < OUTPUT_SLOTS && !remaining.isEmpty(); i++) {
                    remaining = outputHandler.insertItem(i, remaining, false);
                }
                if (!remaining.isEmpty()) {
                    Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), remaining);
                }
            }
        }

        level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        storedEnergy -= EU_PER_BLOCK;
        mineY--;
        setChanged();
    }

    @Override
    public double injectEnergy(final Direction directionFrom, final double amount, final int tier) {
        if (amount <= 0.0D) return amount;
        double space = ENERGY_CAPACITY - storedEnergy;
        double accepted = Math.min(amount, space);
        storedEnergy += accepted;
        setChanged();
        return amount - accepted;
    }

    @Override
    public double getCapacity() { return ENERGY_CAPACITY; }
    @Override
    public double getStoredEnergy() { return storedEnergy; }
    @Override
    public int getTier() { return TIER; }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("StoredEnergy", storedEnergy);
        tag.putInt("MineY", mineY);
        tag.putBoolean("Done", done);
        tag.put("Output", outputHandler.serializeNBT());
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        storedEnergy = Math.min(tag.getDouble("StoredEnergy"), ENERGY_CAPACITY);
        mineY = tag.contains("MineY") ? tag.getInt("MineY") : worldPosition.getY() - 1;
        done = tag.getBoolean("Done");
        if (tag.contains("Output")) {
            outputHandler.deserializeNBT(tag.getCompound("Output"));
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(final @NotNull Capability<T> capability,
                                                       final @Nullable Direction side) {
        if (capability == ModCapabilities.ENERGY_NODE_CAPABILITY) return energyOptional.cast();
        if (capability == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER) {
            return outputOptional.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
        outputOptional.invalidate();
    }

}
