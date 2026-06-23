package dev.ic2port.util;

import dev.ic2port.item.IElectricItem;
import dev.ic2port.item.MiningLaserItem;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Drill, scanner and pipe rules for the {@link dev.ic2port.blockentity.MinerBlockEntity}.
 */
public final class MinerHelper {

    public enum ScannerMode {
        /** Vertical shaft, all mineable blocks. */
        NONE,
        /** Skip non-ore blocks in the column. */
        ORE_ONLY,
        /** Quarry mode — mine all blocks at double speed. */
        QUARRY
    }

    public record DrillProfile(int intervalTicks, float maxHardness, double drillEuPerBlock) {}

    private MinerHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    @Nullable
    public static DrillProfile getDrillProfile(final ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        if (stack.is(ItemRegistry.BASIC_DRILL.get())) {
            return new DrillProfile(24, 3.0F, 50.0D);
        }
        if (stack.is(ItemRegistry.DIAMOND_DRILL.get())) {
            return new DrillProfile(18, 5.0F, 80.0D);
        }
        if (stack.is(ItemRegistry.ADVANCED_DRILL.get())) {
            return new DrillProfile(12, 5.0F, 120.0D);
        }
        if (stack.is(ItemRegistry.MINING_LASER.get())) {
            return new DrillProfile(8, 50.0F, 200.0D);
        }
        return null;
    }

    public static boolean isValidDrill(final ItemStack stack) {
        return getDrillProfile(stack) != null;
    }

    public static boolean isValidScanner(final ItemStack stack) {
        return stack.is(ItemRegistry.OD_SCANNER.get()) || stack.is(ItemRegistry.OV_SCANNER.get());
    }

    public static boolean isMiningPipe(final ItemStack stack) {
        return stack.is(ItemRegistry.MINING_PIPE.get());
    }

    public static ScannerMode getScannerMode(final ItemStack scannerStack) {
        if (scannerStack.is(ItemRegistry.OD_SCANNER.get())) {
            return ScannerMode.ORE_ONLY;
        }
        if (scannerStack.is(ItemRegistry.OV_SCANNER.get())) {
            return ScannerMode.QUARRY;
        }
        return ScannerMode.NONE;
    }

    public static int getMineInterval(final DrillProfile profile, final ScannerMode scannerMode) {
        int interval = profile.intervalTicks();
        if (scannerMode == ScannerMode.QUARRY) {
            interval = Math.max(4, interval / 2);
        }
        return interval;
    }

    /**
     * Block positions mined in one cycle at the current depth.
     */
    public static java.util.List<BlockPos> getLayerPositions(final BlockPos shaftCenter, final ScannerMode scannerMode) {
        if (scannerMode == ScannerMode.QUARRY) {
            java.util.List<BlockPos> positions = new java.util.ArrayList<>(9);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    positions.add(shaftCenter.offset(dx, 0, dz));
                }
            }
            return positions;
        }
        return java.util.List.of(shaftCenter);
    }

    public static int getBlocksPerCycle(final ScannerMode scannerMode) {
        return scannerMode == ScannerMode.QUARRY ? 9 : 1;
    }

    public static boolean shouldMineBlock(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final ScannerMode scannerMode,
            final float maxHardness) {
        if (state.isAir() || !state.getFluidState().isEmpty()) {
            return false;
        }
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0.0F || hardness > maxHardness) {
            return false;
        }
        if (scannerMode == ScannerMode.ORE_ONLY) {
            return OreScannerHelper.isOreBlock(state);
        }
        return true;
    }

    public static boolean drainDrillEnergy(final ItemStack drillStack, final double amount) {
        if (!(drillStack.getItem() instanceof IElectricItem electric)) {
            return true;
        }
        return electric.drawEnergy(drillStack, amount) >= amount;
    }
}
