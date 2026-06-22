package dev.ic2port.util;

import dev.ic2port.block.ICableBlock;
import net.minecraft.world.level.block.Block;

/**
 * Shared connection rules for insulated cable blocks.
 */
public final class CableConnectionHelper {

    private CableConnectionHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isCableBlock(final Block block) {
        return block instanceof ICableBlock;
    }
}
