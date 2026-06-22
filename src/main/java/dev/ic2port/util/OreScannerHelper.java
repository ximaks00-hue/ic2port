package dev.ic2port.util;

import dev.ic2port.setup.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Scans underground blocks for ore-like targets and builds a summary for OD/OV scanners.
 */
public final class OreScannerHelper {

    public static final int HORIZONTAL_RADIUS = 4;
    public static final int DEPTH = 32;
    public static final int OV_HORIZONTAL_RADIUS = 8;
    public static final int OV_DEPTH = 64;

    public record ScanResult(Map<String, Integer> counts, int maxVeinSize, String dominantOreKey) {}

    private OreScannerHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Map<String, Integer> scanColumn(final Level level, final BlockPos origin) {
        return scanArea(level, origin, HORIZONTAL_RADIUS, DEPTH);
    }

    public static ScanResult scanDetailed(final Level level, final BlockPos origin) {
        Map<String, Integer> counts = scanArea(level, origin, OV_HORIZONTAL_RADIUS, OV_DEPTH);
        int maxVein = 0;
        String dominant = null;
        for (var entry : counts.entrySet()) {
            if (entry.getValue() > maxVein) {
                maxVein = entry.getValue();
                dominant = entry.getKey();
            }
        }
        return new ScanResult(counts, maxVein, dominant);
    }

    private static Map<String, Integer> scanArea(final Level level, final BlockPos origin,
                                                  final int radius, final int depth) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minY = Math.max(level.getMinBuildHeight(), origin.getY() - depth);
        for (int x = origin.getX() - radius; x <= origin.getX() + radius; x++) {
            for (int z = origin.getZ() - radius; z <= origin.getZ() + radius; z++) {
                for (int y = origin.getY() - 1; y >= minY; y--) {
                    cursor.set(x, y, z);
                    if (!level.isLoaded(cursor)) continue;
                    BlockState state = level.getBlockState(cursor);
                    String label = labelFor(state.getBlock());
                    if (label != null) {
                        counts.merge(label, 1, Integer::sum);
                    }
                }
            }
        }
        return counts;
    }

    public static Component formatResult(final Map<String, Integer> counts) {
        if (counts.isEmpty()) {
            return Component.translatable("message.ic2port.od_scanner.empty");
        }
        MutableComponent message = Component.translatable("message.ic2port.od_scanner.header");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (!first) message.append(", ");
            first = false;
            message.append(Component.translatable(entry.getKey()).append(": " + entry.getValue()));
        }
        return message;
    }

    private static String labelFor(final Block block) {
        if (block == BlockRegistry.TIN_ORE.get() || block == BlockRegistry.DEEPSLATE_TIN_ORE.get()) {
            return "block.ic2port.tin_ore";
        }
        if (block == BlockRegistry.URANIUM_ORE.get() || block == BlockRegistry.DEEPSLATE_URANIUM_ORE.get()) {
            return "block.ic2port.uranium_ore";
        }
        if (block == net.minecraft.world.level.block.Blocks.COAL_ORE
                || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_COAL_ORE) {
            return "block.minecraft.coal_ore";
        }
        if (block == net.minecraft.world.level.block.Blocks.IRON_ORE
                || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_IRON_ORE) {
            return "block.minecraft.iron_ore";
        }
        if (block == net.minecraft.world.level.block.Blocks.COPPER_ORE
                || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_COPPER_ORE) {
            return "block.minecraft.copper_ore";
        }
        if (block == net.minecraft.world.level.block.Blocks.GOLD_ORE
                || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_GOLD_ORE) {
            return "block.minecraft.gold_ore";
        }
        if (block == net.minecraft.world.level.block.Blocks.LAPIS_ORE
                || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_LAPIS_ORE) {
            return "block.minecraft.lapis_ore";
        }
        if (block == net.minecraft.world.level.block.Blocks.REDSTONE_ORE
                || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_REDSTONE_ORE) {
            return "block.minecraft.redstone_ore";
        }
        if (block == net.minecraft.world.level.block.Blocks.DIAMOND_ORE
                || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_DIAMOND_ORE) {
            return "block.minecraft.diamond_ore";
        }
        if (block == net.minecraft.world.level.block.Blocks.EMERALD_ORE
                || block == net.minecraft.world.level.block.Blocks.DEEPSLATE_EMERALD_ORE) {
            return "block.minecraft.emerald_ore";
        }
        return null;
    }
}
