package dev.ic2port.util;

import dev.ic2port.blockentity.CropSticksBlockEntity;
import dev.ic2port.crop.CropRegistry;
import dev.ic2port.item.HydrationCellItem;
import dev.ic2port.item.WeedExItem;
import dev.ic2port.setup.BlockRegistry;
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
 * Scans use block-state checks before {@code getBlockEntity} to reduce chunk lookups.
 */
public final class CropMatronHelper {

    public static final int HORIZONTAL_RADIUS = 4;
    public static final int VERTICAL_RADIUS = 2;

    private static final Block CROP_STICKS_BLOCK = BlockRegistry.CROP_STICKS.get();
    private static final BlockPos[] SCAN_OFFSETS = buildScanOffsets();

    private CropMatronHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static int hydrateFarmland(final Level level, final BlockPos center) {
        int count = 0;
        final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (final BlockPos offset : SCAN_OFFSETS) {
            mutable.setWithOffset(center, offset);
            final BlockState state = level.getBlockState(mutable);
            if (!state.is(Blocks.FARMLAND) || !state.hasProperty(FarmBlock.MOISTURE)) {
                continue;
            }
            if (state.getValue(FarmBlock.MOISTURE) >= FarmBlock.MAX_MOISTURE) {
                continue;
            }
            level.setBlock(mutable, state.setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE), Block.UPDATE_ALL);
            count++;
        }
        return count;
    }

    public static int clearWeeds(final Level level, final BlockPos center) {
        int cleared = 0;
        final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (final BlockPos offset : SCAN_OFFSETS) {
            mutable.setWithOffset(center, offset);
            if (!level.getBlockState(mutable).is(CROP_STICKS_BLOCK)) {
                continue;
            }
            final BlockEntity blockEntity = level.getBlockEntity(mutable);
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
        final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (final BlockPos offset : SCAN_OFFSETS) {
            mutable.setWithOffset(center, offset);
            if (!level.getBlockState(mutable).is(CROP_STICKS_BLOCK)) {
                continue;
            }
            final BlockEntity blockEntity = level.getBlockEntity(mutable);
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

    private static BlockPos[] buildScanOffsets() {
        final int volume = (HORIZONTAL_RADIUS * 2 + 1)
                * (HORIZONTAL_RADIUS * 2 + 1)
                * (VERTICAL_RADIUS * 2 + 1);
        final BlockPos[] offsets = new BlockPos[volume];
        int index = 0;
        for (int dx = -HORIZONTAL_RADIUS; dx <= HORIZONTAL_RADIUS; dx++) {
            for (int dy = -VERTICAL_RADIUS; dy <= VERTICAL_RADIUS; dy++) {
                for (int dz = -HORIZONTAL_RADIUS; dz <= HORIZONTAL_RADIUS; dz++) {
                    offsets[index++] = new BlockPos(dx, dy, dz);
                }
            }
        }
        return offsets;
    }
}
