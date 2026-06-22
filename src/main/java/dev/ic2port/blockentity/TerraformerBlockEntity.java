package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.api.energy.IEnergyNode;
import dev.ic2port.item.TerraformerBlueprintItem;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ModCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * HV Terraformer — converts terrain in a 9×1×9 area based on installed blueprint.
 * Consumes 10 EU/t while active.
 */
public class TerraformerBlockEntity extends BlockEntity implements IEnergyAcceptor {

    public static final double ENERGY_CAPACITY = 100_000.0D;
    public static final int TIER = EnergyTier.HV;
    public static final double EU_PER_TICK = 10.0D;
    public static final int RANGE = 4;
    private static final int WORK_INTERVAL = 20;
    private static final double EU_PER_OPERATION = EU_PER_TICK * WORK_INTERVAL;

    public static final int SLOT_BLUEPRINT = 0;
    public static final int SLOT_COUNT = 1;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override protected void onContentsChanged(final int slot) { setChanged(); }
    };
    private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> itemHandler);
    private final LazyOptional<IEnergyNode> energyOptional = LazyOptional.of(() -> this);

    private double storedEnergy;
    private int tickCount;
    private int scanX = -RANGE;
    private int scanZ = -RANGE;

    public TerraformerBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.TERRAFORMER_BE.get(), pos, state);
    }

    public static void serverTick(final Level level, final BlockPos pos, final BlockState state,
                                   final TerraformerBlockEntity entity) {
        entity.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide || storedEnergy < EU_PER_OPERATION) return;

        tickCount++;
        if (tickCount < WORK_INTERVAL) return;
        tickCount = 0;

        ItemStack blueprint = itemHandler.getStackInSlot(SLOT_BLUEPRINT);
        if (!(blueprint.getItem() instanceof TerraformerBlueprintItem tf)) return;

        BlockPos target = worldPosition.offset(scanX, 0, scanZ);
        if (applyBlueprint(tf.getMode(), target)) {
            storedEnergy -= EU_PER_OPERATION;
            setChanged();
        }

        scanX++;
        if (scanX > RANGE) {
            scanX = -RANGE;
            scanZ++;
            if (scanZ > RANGE) {
                scanZ = -RANGE;
            }
        }
    }

    private boolean applyBlueprint(final TerraformerBlueprintItem.Mode mode, final BlockPos target) {
        BlockPos surface = findSurface(target);
        if (surface == null) return false;
        switch (mode) {
            case CULTIVATION -> {
                BlockState below = level.getBlockState(surface);
                if (below.is(Blocks.DIRT) || below.is(Blocks.GRASS_BLOCK)) {
                    level.setBlock(surface, Blocks.FARMLAND.defaultBlockState(),
                            net.minecraft.world.level.block.Block.UPDATE_ALL);
                    return true;
                }
            }
            case IRRIGATION -> {
                BlockState below = level.getBlockState(surface);
                if (below.is(Blocks.DIRT)) {
                    level.setBlock(surface, Blocks.MUD.defaultBlockState(),
                            net.minecraft.world.level.block.Block.UPDATE_ALL);
                    return true;
                }
            }
            case DESERTIFICATION -> {
                BlockState below = level.getBlockState(surface);
                if (below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.DIRT)) {
                    level.setBlock(surface, Blocks.SAND.defaultBlockState(),
                            net.minecraft.world.level.block.Block.UPDATE_ALL);
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    private BlockPos findSurface(final BlockPos xz) {
        for (int y = worldPosition.getY() + 5; y >= worldPosition.getY() - 5; y--) {
            BlockPos candidate = new BlockPos(xz.getX(), y, xz.getZ());
            if (!level.getBlockState(candidate).isAir() && level.getBlockState(candidate.above()).isAir()) {
                return candidate;
            }
        }
        return null;
    }

    @Override
    public double injectEnergy(final Direction directionFrom, final double amount, final int tier) {
        if (level == null || level.isClientSide || amount <= 0.0D) {
            return amount;
        }
        if (tier > getTier()) {
            return amount;
        }
        double space = ENERGY_CAPACITY - storedEnergy;
        double accepted = Math.min(amount, space);
        storedEnergy += accepted;
        setChanged();
        return amount - accepted;
    }

    @Override public double getCapacity() { return ENERGY_CAPACITY; }
    @Override public double getStoredEnergy() { return storedEnergy; }
    @Override public int getTier() { return TIER; }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("StoredEnergy", storedEnergy);
        tag.put("Items", itemHandler.serializeNBT());
        tag.putInt("ScanX", scanX);
        tag.putInt("ScanZ", scanZ);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        storedEnergy = Math.min(tag.getDouble("StoredEnergy"), ENERGY_CAPACITY);
        if (tag.contains("Items")) itemHandler.deserializeNBT(tag.getCompound("Items"));
        scanX = tag.contains("ScanX") ? tag.getInt("ScanX") : -RANGE;
        scanZ = tag.contains("ScanZ") ? tag.getInt("ScanZ") : -RANGE;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(final @NotNull Capability<T> cap,
                                                       final @Nullable Direction side) {
        if (cap == ModCapabilities.ENERGY_NODE_CAPABILITY) return energyOptional.cast();
        if (cap == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER) return itemOptional.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
        itemOptional.invalidate();
    }
}
