package dev.ic2port.util;

import dev.ic2port.tube.TransportedItem;
import net.minecraft.core.Direction;

/**
 * IC2-style vertical gravity modifiers for in-flight tube items.
 */
public final class TubeGravityHelper {

    public static final byte MIN_SPEED = 4;
    public static final byte MAX_SPEED = 32;

    private TubeGravityHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void applyGravity(final TransportedItem item) {
        if (item.isHovering()) {
            return;
        }
        Direction travel = item.getTravelDirection();
        byte speed = item.getSpeed();
        if (travel == Direction.DOWN) {
            item.setSpeed((byte) Math.min(MAX_SPEED, speed + 4));
        } else if (travel == Direction.UP) {
            item.setSpeed((byte) Math.max(MIN_SPEED, speed - 4));
        }
    }
}
