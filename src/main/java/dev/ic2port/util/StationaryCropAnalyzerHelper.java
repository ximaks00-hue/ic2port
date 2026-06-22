package dev.ic2port.util;

import dev.ic2port.blockentity.CropSticksBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;

/**
 * Area scan helpers for the stationary {@link dev.ic2port.blockentity.CropAnalyzerBlockEntity}.
 */
public final class StationaryCropAnalyzerHelper {

    public static final int HORIZONTAL_RADIUS = 4;
    public static final int VERTICAL_RADIUS = 2;
    public static final double ENERGY_PER_SCAN_LEVEL = 2500.0D;
    public static final double ENERGY_PER_TICK = 1.0D;

    private StationaryCropAnalyzerHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    @Nullable
    public static CropSticksBlockEntity findNextCrop(final Level level, final BlockPos center) {
        CropSticksBlockEntity best = null;
        double bestScore = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-HORIZONTAL_RADIUS, -VERTICAL_RADIUS, -HORIZONTAL_RADIUS),
                center.offset(HORIZONTAL_RADIUS, VERTICAL_RADIUS, HORIZONTAL_RADIUS))) {
            if (pos.equals(center)) {
                continue;
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof CropSticksBlockEntity crop)) {
                continue;
            }
            if (crop.getCrop() == null || crop.getScanLevel() >= 4) {
                continue;
            }
            double score = center.distSqr(pos) + (4 - crop.getScanLevel()) * 0.01D;
            if (score < bestScore) {
                bestScore = score;
                best = crop;
            }
        }
        return best;
    }

    public static boolean advanceScan(final CropSticksBlockEntity crop) {
        if (crop.getCrop() == null) {
            return false;
        }
        if (crop.getScanLevel() >= 4) {
            return false;
        }
        crop.setScanLevel(crop.getScanLevel() + 1);
        crop.setChanged();
        return true;
    }
}
