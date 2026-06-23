package dev.ic2port.util;

import dev.ic2port.setup.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Scans underground blocks for ore-like targets and builds a summary for OD/OV scanners.
 */
public final class OreScannerHelper {

    public static final int HORIZONTAL_RADIUS = 4;
    public static final int DEPTH = 32;
    public static final int OV_HORIZONTAL_RADIUS = 8;
    public static final int OV_DEPTH = 64;

    private static final BlockPos[] NEIGHBORS = {
            new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0),
            new BlockPos(0, 1, 0), new BlockPos(0, -1, 0),
            new BlockPos(0, 0, 1), new BlockPos(0, 0, -1)
    };

    public record ScanResult(Map<String, Integer> counts, int maxVeinSize, String dominantOreKey) {}

    private OreScannerHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Map<String, Integer> scanColumn(final Level level, final BlockPos origin) {
        return scanArea(level, origin, HORIZONTAL_RADIUS, DEPTH).counts();
    }

    public static ScanResult scanDetailed(final Level level, final BlockPos origin) {
        AreaScan scan = scanArea(level, origin, OV_HORIZONTAL_RADIUS, OV_DEPTH);
        Map<String, Integer> counts = scan.counts();
        if (counts.isEmpty()) {
            return new ScanResult(counts, 0, null);
        }

        String dominant = null;
        int maxTotal = 0;
        for (var entry : counts.entrySet()) {
            if (entry.getValue() > maxTotal) {
                maxTotal = entry.getValue();
                dominant = entry.getKey();
            }
        }

        int maxVein = dominant == null ? 0 : largestVeinSize(scan.positionsByLabel().getOrDefault(dominant, Set.of()));
        return new ScanResult(counts, maxVein, dominant);
    }

    private record AreaScan(Map<String, Integer> counts, Map<String, Set<BlockPos>> positionsByLabel) {}

    private static AreaScan scanArea(final Level level, final BlockPos origin,
                                     final int radius, final int depth) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, Set<BlockPos>> positionsByLabel = new HashMap<>();
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
                        positionsByLabel.computeIfAbsent(label, key -> new HashSet<>()).add(cursor.immutable());
                    }
                }
            }
        }
        return new AreaScan(counts, positionsByLabel);
    }

    private static int largestVeinSize(final Set<BlockPos> orePositions) {
        if (orePositions.isEmpty()) {
            return 0;
        }

        Set<BlockPos> remaining = new HashSet<>(orePositions);
        int largest = 0;

        while (!remaining.isEmpty()) {
            BlockPos start = remaining.iterator().next();
            int size = 0;
            Queue<BlockPos> queue = new ArrayDeque<>();
            queue.add(start);
            remaining.remove(start);

            while (!queue.isEmpty()) {
                BlockPos current = queue.poll();
                size++;
                for (BlockPos offset : NEIGHBORS) {
                    BlockPos neighbor = current.offset(offset);
                    if (remaining.remove(neighbor)) {
                        queue.add(neighbor);
                    }
                }
            }
            largest = Math.max(largest, size);
        }
        return largest;
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

    public static boolean isOreBlock(final BlockState state) {
        return labelFor(state.getBlock()) != null;
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
}
