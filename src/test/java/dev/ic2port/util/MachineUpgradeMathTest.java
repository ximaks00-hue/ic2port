package dev.ic2port.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MachineUpgradeMathTest {

    @Test
    void overclockerReducesProcessTime() {
        assertEquals(100, MachineUpgradeMath.scaledProcessTime(100, 0));
        assertEquals(70, MachineUpgradeMath.scaledProcessTime(100, 1));
        assertEquals(49, MachineUpgradeMath.scaledProcessTime(100, 2));
        assertEquals(1, MachineUpgradeMath.scaledProcessTime(2, 10));
    }

    @Test
    void overclockerIncreasesEnergyPerTick() {
        assertEquals(10.0D, MachineUpgradeMath.scaledEnergyPerTick(10.0D, 0));
        assertEquals(16.0D, MachineUpgradeMath.scaledEnergyPerTick(10.0D, 1), 0.001D);
        assertEquals(25.6D, MachineUpgradeMath.scaledEnergyPerTick(10.0D, 2), 0.001D);
    }
}
