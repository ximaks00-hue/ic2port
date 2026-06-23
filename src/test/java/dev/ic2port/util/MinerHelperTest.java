package dev.ic2port.util;

import dev.ic2port.util.MinerHelper.DrillProfile;
import dev.ic2port.util.MinerHelper.ScannerMode;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MinerHelperTest {

    @Test
    void quarryModeHalvesIntervalWithMinimum() {
        DrillProfile profile = new DrillProfile(24, 5.0F, 80.0D);
        assertEquals(12, MinerHelper.getMineInterval(profile, ScannerMode.QUARRY));
        assertEquals(4, MinerHelper.getMineInterval(new DrillProfile(8, 50.0F, 200.0D), ScannerMode.QUARRY));
    }

    @Test
    void defaultModeKeepsDrillInterval() {
        DrillProfile profile = new DrillProfile(18, 5.0F, 80.0D);
        assertEquals(18, MinerHelper.getMineInterval(profile, ScannerMode.NONE));
        assertEquals(18, MinerHelper.getMineInterval(profile, ScannerMode.ORE_ONLY));
    }

    @Test
    void quarryLayerUsesThreeByThreeFootprint() {
        BlockPos center = new BlockPos(10, 64, 10);
        assertEquals(9, MinerHelper.getLayerPositions(center, ScannerMode.QUARRY).size());
        assertEquals(center, MinerHelper.getLayerPositions(center, ScannerMode.QUARRY).get(4));
    }

    @Test
    void singleColumnModesMineCenterOnly() {
        BlockPos center = new BlockPos(4, 32, 4);
        assertEquals(1, MinerHelper.getLayerPositions(center, ScannerMode.NONE).size());
        assertEquals(center, MinerHelper.getLayerPositions(center, ScannerMode.ORE_ONLY).get(0));
        assertEquals(1, MinerHelper.getBlocksPerCycle(ScannerMode.ORE_ONLY));
        assertEquals(9, MinerHelper.getBlocksPerCycle(ScannerMode.QUARRY));
    }
}
