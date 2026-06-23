package dev.ic2port.util;

import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TubeRoutingHelperTest {

    @Test
    void roundRobinReturnsSingleDirection() {
        List<Direction> directions = TubeRoutingHelper.roundRobinDirection(Direction.NORTH, 2);
        assertEquals(1, directions.size());
        assertFalse(directions.contains(Direction.NORTH));
    }
}
