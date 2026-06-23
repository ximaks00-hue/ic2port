package dev.ic2port.energy;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldEnergyNetTest {

    @Test
    void floodFillGroupsOrthogonallyConnectedConductors() {
        Set<BlockPos> conductors = Set.of(
                new BlockPos(0, 0, 0),
                new BlockPos(1, 0, 0),
                new BlockPos(2, 0, 0),
                new BlockPos(0, 0, 5));

        Predicate<BlockPos> isConductor = conductors::contains;

        Set<BlockPos> line = WorldEnergyNet.floodFill(new BlockPos(0, 0, 0), isConductor);
        assertEquals(3, line.size());
        assertTrue(line.contains(new BlockPos(2, 0, 0)));

        Set<BlockPos> isolated = WorldEnergyNet.floodFill(new BlockPos(0, 0, 5), isConductor);
        assertEquals(1, isolated.size());
    }

    @Test
    void floodFillCrossesChunkBoundariesWhenPredicateAllows() {
        Set<BlockPos> conductors = new HashSet<>();
        conductors.add(new BlockPos(15, 64, 0));
        conductors.add(new BlockPos(16, 64, 0));

        Set<BlockPos> grid = WorldEnergyNet.floodFill(new BlockPos(15, 64, 0), conductors::contains);
        assertEquals(2, grid.size());
    }

    @Test
    void floodFillDoesNotCrossDiagonalGaps() {
        Set<BlockPos> conductors = Set.of(
                new BlockPos(0, 64, 0),
                new BlockPos(1, 65, 0),
                new BlockPos(1, 64, 1));

        Set<BlockPos> grid = WorldEnergyNet.floodFill(new BlockPos(0, 64, 0), conductors::contains);
        assertEquals(1, grid.size());
        assertTrue(grid.contains(new BlockPos(0, 64, 0)));
    }

    @Test
    void adjacentPositionsReturnsExactlySixOrthogonalNeighbors() {
        BlockPos origin = new BlockPos(10, 20, 30);
        Set<BlockPos> adjacent = WorldEnergyNet.adjacentPositions(origin);

        assertEquals(6, adjacent.size());
        assertTrue(adjacent.contains(origin.north()));
        assertTrue(adjacent.contains(origin.south()));
        assertTrue(adjacent.contains(origin.east()));
        assertTrue(adjacent.contains(origin.west()));
        assertTrue(adjacent.contains(origin.above()));
        assertTrue(adjacent.contains(origin.below()));
    }

    @Test
    void drainActiveSetReturnsSnapshotAndClearsSource() {
        Set<BlockPos> active = new HashSet<>(Set.of(
                new BlockPos(0, 0, 0),
                new BlockPos(1, 0, 0),
                new BlockPos(2, 0, 0)));

        Set<BlockPos> snapshot = WorldEnergyNet.drainActiveSet(active);

        assertEquals(3, snapshot.size());
        assertTrue(active.isEmpty());
        assertTrue(snapshot.contains(new BlockPos(1, 0, 0)));
    }
}
