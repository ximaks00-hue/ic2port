package dev.ic2port.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReactorMathTest {

    @Test
    void pulsesForNeighborsAddsAdjacentFuelRods() {
        assertEquals(1, ReactorMath.pulsesForNeighbors(0));
        assertEquals(3, ReactorMath.pulsesForNeighbors(2));
        assertEquals(5, ReactorMath.pulsesForNeighbors(4));
    }

    @Test
    void energyForPulsesScalesLinearly() {
        assertEquals(5.0D, ReactorMath.energyForPulses(1));
        assertEquals(15.0D, ReactorMath.energyForPulses(3));
    }

    @Test
    void heatForPulsesUsesTriangularGrowth() {
        assertEquals(4.0D, ReactorMath.heatForPulses(1));
        assertEquals(24.0D, ReactorMath.heatForPulses(3));
    }

    @Test
    void moxEnergyMultiplierIncreasesWithHeatRatio() {
        assertEquals(1.0D, ReactorMath.moxEnergyMultiplier(0.0D));
        assertEquals(1.375D, ReactorMath.moxEnergyMultiplier(0.5D), 0.001D);
        assertEquals(2.5D, ReactorMath.moxEnergyMultiplier(1.0D), 0.001D);
    }

    @Test
    void moxHeatMultiplierIncreasesWithHeatRatio() {
        assertEquals(1.0D, ReactorMath.moxHeatMultiplier(0.0D));
        assertEquals(1.375D, ReactorMath.moxHeatMultiplier(0.5D), 0.001D);
        assertEquals(1.75D, ReactorMath.moxHeatMultiplier(1.0D), 0.001D);
    }
}
