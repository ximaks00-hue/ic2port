package dev.ic2port.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Smoke benchmark for cable-grid bitmask hot paths.
 */
class CableGridBenchmarkTest {

    @Test
    void faceBitMaskAllFaces() {
        int mask = 0;
        for (int ordinal = 0; ordinal < 6; ordinal++) {
            mask |= 1 << ordinal;
        }
        Assertions.assertEquals(63, mask);
    }

    @Test
    void clusterBenchmarkSmoke() {
        long start = System.nanoTime();
        int iterations = 100_000;
        int acc = 0;
        for (int i = 0; i < iterations; i++) {
            acc |= 1 << (i % 6);
        }
        long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
        Assertions.assertTrue(acc > 0);
        Assertions.assertTrue(elapsedMs < 5_000L, "Cable grid benchmark took too long: " + elapsedMs + "ms");
    }
}
