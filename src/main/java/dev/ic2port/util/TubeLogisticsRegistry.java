package dev.ic2port.util;

import dev.ic2port.blockentity.TubeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Active logistic requests broadcast by {@link dev.ic2port.block.RequestTubeBlock} nodes.
 */
public final class TubeLogisticsRegistry {

    public record RequestEntry(ItemStack filter, @Nullable DyeColor color, BlockPos requester) {
    }

    private static final Map<ResourceKey<Level>, Map<BlockPos, RequestEntry>> ACTIVE_REQUESTS = new HashMap<>();

    private TubeLogisticsRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void updateRequest(
            final Level level,
            final BlockPos requester,
            final ItemStack filter,
            @Nullable final DyeColor color,
            final boolean active) {
        Map<BlockPos, RequestEntry> requests = ACTIVE_REQUESTS.computeIfAbsent(
                level.dimension(),
                ignored -> new HashMap<>());
        if (active && !filter.isEmpty()) {
            requests.put(requester.immutable(), new RequestEntry(filter.copyWithCount(1), color, requester.immutable()));
            return;
        }
        requests.remove(requester);
        if (requests.isEmpty()) {
            ACTIVE_REQUESTS.remove(level.dimension());
        }
    }

    public static List<RequestEntry> getRequests(final Level level) {
        Map<BlockPos, RequestEntry> requests = ACTIVE_REQUESTS.get(level.dimension());
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(requests.values());
    }

    public static void clearRequester(final TubeBlockEntity tube) {
        if (tube.getLevel() == null) {
            return;
        }
        updateRequest(tube.getLevel(), tube.getBlockPos(), ItemStack.EMPTY, null, false);
    }
}
