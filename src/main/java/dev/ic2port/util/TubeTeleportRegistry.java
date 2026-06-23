package dev.ic2port.util;

import dev.ic2port.blockentity.TubeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Cross-dimensional registry for teleport tube receivers.
 */
public final class TubeTeleportRegistry {

    private record Endpoint(ResourceKey<Level> dimension, BlockPos pos) {
        private boolean matches(final TubeBlockEntity tube) {
            if (tube.getLevel() == null) {
                return false;
            }
            return tube.getLevel().dimension().equals(dimension) && tube.getBlockPos().equals(pos);
        }
    }

    private static final Map<Integer, Set<Endpoint>> RECEIVERS = new HashMap<>();

    private TubeTeleportRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void register(final TubeBlockEntity tube) {
        if (!tube.isTeleportReceive()) {
            return;
        }
        RECEIVERS.computeIfAbsent(tube.getTeleportNetworkId(), ignored -> new HashSet<>())
                .add(new Endpoint(tube.getLevel().dimension(), tube.getBlockPos()));
    }

    public static void unregister(final TubeBlockEntity tube) {
        Set<Endpoint> endpoints = RECEIVERS.get(tube.getTeleportNetworkId());
        if (endpoints == null) {
            return;
        }
        endpoints.removeIf(endpoint -> endpoint.matches(tube));
        if (endpoints.isEmpty()) {
            RECEIVERS.remove(tube.getTeleportNetworkId());
        }
    }

    @Nullable
    public static TubeBlockEntity findReceiver(
            final Level fromLevel,
            final int networkId,
            final BlockPos exclude) {
        Set<Endpoint> endpoints = RECEIVERS.get(networkId);
        if (endpoints == null || endpoints.isEmpty()) {
            return null;
        }
        MinecraftServer server = fromLevel.getServer();
        if (server == null) {
            return null;
        }
        List<Endpoint> candidates = new ArrayList<>(endpoints);
        int start = Math.floorMod(exclude.hashCode(), candidates.size());
        for (int offset = 0; offset < candidates.size(); offset++) {
            Endpoint endpoint = candidates.get((start + offset) % candidates.size());
            if (endpoint.dimension().equals(fromLevel.dimension()) && endpoint.pos().equals(exclude)) {
                continue;
            }
            Level level = server.getLevel(endpoint.dimension());
            if (level == null) {
                continue;
            }
            if (!(level.getBlockEntity(endpoint.pos()) instanceof TubeBlockEntity tube)) {
                continue;
            }
            if (!tube.isTeleportReceive()) {
                continue;
            }
            return tube;
        }
        return null;
    }
}
