package dev.ic2port.util;

import dev.ic2port.blockentity.CropSticksBlockEntity;
import dev.ic2port.crop.CropRegistry;
import dev.ic2port.item.HydrationCellItem;
import dev.ic2port.item.WeedExItem;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Area helpers for the {@link dev.ic2port.blockentity.CropmatronBlockEntity}.
 */
public final class CropMatronHelper {

    public static final int HORIZONTAL_RADIUS = 4;
    public static final int VERTICAL_RADIUS = 2;

    private CropMatronHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static int hydrateFarmland(final Level level, final BlockPos center) {
        int count = 0;
        for (BlockPos pos : scanPositions(center)) {
            BlockState state = level.getBlockState(pos);
            if (!state.is(Blocks.FARMLAND) || !state.hasProperty(FarmBlock.MOISTURE)) {
                continue;
            }
            if (state.getValue(FarmBlock.MOISTURE) >= FarmBlock.MAX_MOISTURE) {
                continue;
            }
            level.setBlock(pos, state.setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE), Block.UPDATE_CLIENTS);
            count++;
        }
        return count;
    }

    public static int clearWeeds(final Level level, final BlockPos center) {
        int cleared = 0;
        for (BlockPos pos : scanPositions(center)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CropSticksBlockEntity crop && crop.getCrop() == CropRegistry.WEED) {
                if (crop.tryClearWeed()) {
                    cleared++;
                }
            }
        }
        return cleared;
    }

    public static int applySupply(
            final Level level,
            final BlockPos center,
            final ItemStack supplyStack,
            final int fertilizerBoost) {
        int tended = 0;
        for (BlockPos pos : scanPositions(center)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof CropSticksBlockEntity crop)) {
                continue;
            }
            if (crop.getCrop() == null || crop.getCrop() == CropRegistry.WEED) {
                continue;
            }
            if (supplyStack.is(ItemRegistry.FERTILIZER.get()) && crop.tryFertilize(fertilizerBoost)) {
                tended++;
            } else if (supplyStack.is(ItemRegistry.HYDRATION_CELL.get())
                    && crop.tryApplyHydration(HydrationCellItem.APPLY_AMOUNT)) {
                tended++;
            } else if (supplyStack.is(ItemRegistry.WEED_EX.get()) && crop.tryApplyWeedEx(WeedExItem.APPLY_AMOUNT)) {
                tended++;
            }
        }
        return tended;
    }

    private static Iterable<BlockPos> scanPositions(final BlockPos center) {
        return BlockPos.betweenClosed(
                center.offset(-HORIZONTAL_RADIUS, -VERTICAL_RADIUS, -HORIZONTAL_RADIUS),
                center.offset(HORIZONTAL_RADIUS, VERTICAL_RADIUS, HORIZONTAL_RADIUS));
    }
}
