package dev.ic2port.util;

import net.minecraft.core.Direction;

/**
 * Bit masks for per-face pipe/cable connections (1 = open, 0 = wrench-disconnected or covered).
 */
public final class PipeConnectionHelper {

    public static final int ALL_FACES_MASK = 0x3F;

    private PipeConnectionHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static int faceBit(final Direction direction) {
        return 1 << direction.ordinal();
    }

    public static boolean isFaceOpen(final int mask, final Direction direction) {
        return (mask & faceBit(direction)) != 0;
    }

    public static int toggleFace(final int mask, final Direction direction) {
        return mask ^ faceBit(direction);
    }

    public static int setFaceOpen(final int mask, final Direction direction, final boolean open) {
        int bit = faceBit(direction);
        return open ? (mask | bit) : (mask & ~bit);
    }
}
