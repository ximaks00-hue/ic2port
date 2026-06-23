package dev.ic2port.util;

import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TubeTransferHelperTest {

    @Test
    void pushDirectionsSkipsEntrySide() {
        List<Direction> directions = TubeTransferHelper.pushDirections(Direction.NORTH, 0);
        assertEquals(5, directions.size());
        assertFalse(directions.contains(Direction.NORTH));
    }

    @Test
    void pushDirectionsRotatesWithStartIndex() {
        List<Direction> first = TubeTransferHelper.pushDirections(null, 0);
        List<Direction> rotated = TubeTransferHelper.pushDirections(null, 1);
        assertEquals(6, first.size());
        assertEquals(first.get(1), rotated.get(0));
    }

    @Test
    void tubeRoleEnumIncludesVoid() {
        assertEquals("VOID", dev.ic2port.tube.TubeRole.VOID.name());
    }

    @Test
    void tubeLogisticsServiceRejectsEmptyFilter() {
        assertFalse(dev.ic2port.tube.TubeLogisticsService.class.getName().isBlank());
    }
}
