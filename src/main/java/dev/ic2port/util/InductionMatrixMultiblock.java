package dev.ic2port.util;

import dev.ic2port.setup.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Validates a 5×5×5 induction matrix shell around a controller block.
 */
public final class InductionMatrixMultiblock {

    public static final int SIZE = 5;
    public static final int HALF = SIZE / 2;

    private InductionMatrixMultiblock() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isValid(final Level level, final BlockPos controllerPos) {
        BlockPos origin = controllerPos.offset(-HALF, -HALF, -HALF);
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    BlockPos check = origin.offset(x, y, z);
                    BlockState state = level.getBlockState(check);
                    boolean isShell = x == 0 || x == SIZE - 1 || y == 0 || y == SIZE - 1 || z == 0 || z == SIZE - 1;
                    boolean isCenter = x == HALF && y == HALF && z == HALF;

                    if (isCenter) {
                        if (!state.is(BlockRegistry.INDUCTION_MATRIX.get())) {
                            return false;
                        }
                        continue;
                    }
                    if (isShell) {
                        if (!state.is(BlockRegistry.INDUCTION_MATRIX_CASING.get())) {
                            return false;
                        }
                        continue;
                    }
                    if (!state.isAir()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static int countCasingBlocks(final Level level, final BlockPos controllerPos) {
        int count = 0;
        BlockPos origin = controllerPos.offset(-HALF, -HALF, -HALF);
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    BlockPos check = origin.offset(x, y, z);
                    if (level.getBlockState(check).is(BlockRegistry.INDUCTION_MATRIX_CASING.get())) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
}
