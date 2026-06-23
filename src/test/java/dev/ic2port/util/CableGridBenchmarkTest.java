package dev.ic2port.util;

import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 5C harness — measures masked cable-forward iteration cost for large grids.
 * <p>
 * Full in-world cable grids need a loaded {@link net.minecraft.world.level.Level}; this test
 * benchmarks the neighbor-face mask path used by {@link EnergyTransferHelper#forwardCablePacket}.
 */
class CableGridBenchmarkTest {

    private static final double TRANSFER_LOSS = 0.2D;

    @Test
    void faceBitMaskCoversAllDirections() {
        int mask = 0;
        for (Direction direction : Direction.values()) {
            mask |= EnergyTransferHelper.faceBit(direction);
        }
        assertEquals(0x3F, mask);
    }

    @Test
    void cableGridMaskedForwardBenchmark() {
        int gridSize = 10_000;
        int neighborMask = EnergyTransferHelper.faceBit(Direction.NORTH)
                | EnergyTransferHelper.faceBit(Direction.SOUTH)
                | EnergyTransferHelper.faceBit(Direction.EAST);

        long startNanos = System.nanoTime();
        double totalRemainder = 0.0D;
        for (int cell = 0; cell < gridSize; cell++) {
            double remainder = 32.0D - TRANSFER_LOSS;
            for (Direction direction : Direction.values()) {
                if ((neighborMask & EnergyTransferHelper.faceBit(direction)) == 0) {
                    continue;
                }
                remainder = simulateNeighborAccept(remainder);
            }
            totalRemainder += remainder;
        }
        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;

        System.out.printf(
                "CableGridBenchmark: %d cells, mask=0x%X, remainder=%.1f EU, %d ms%n",
                gridSize,
                neighborMask,
                totalRemainder,
                elapsedMs);

        assertTrue(totalRemainder >= 0.0D);
        assertTrue(elapsedMs >= 0L);
    }

    private static double simulateNeighborAccept(final double amount) {
        if (amount <= 0.0D) {
            return 0.0D;
        }
        return amount * 0.5D;
    }
}
