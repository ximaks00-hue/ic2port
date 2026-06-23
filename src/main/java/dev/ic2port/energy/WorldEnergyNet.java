package dev.ic2port.energy;

import dev.ic2port.api.energy.IEnergyConductor;
import dev.ic2port.api.energy.IEnergyNet;
import dev.ic2port.blockentity.BaseCableBlockEntity;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.setup.ModConfig;
import dev.ic2port.util.EnergyTransferHelper;
import dev.ic2port.util.TickProfiler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Per-dimension global EU cable graph — registers conductors, builds connected grids,
 * and ticks only active cables once per level tick (IC2 Classic-style).
 */
public final class WorldEnergyNet implements IEnergyNet {

    private static final Map<ResourceKey<Level>, WorldEnergyNet> NETS = new HashMap<>();

    private final ServerLevel level;
    private final Set<BlockPos> registeredCables = new HashSet<>();
    private final Set<BlockPos> activeCables = new HashSet<>();
    private final Map<BlockPos, EnergyGrid> gridByPos = new HashMap<>();
    private final Map<Integer, EnergyGrid> gridsById = new HashMap<>();
    private int nextGridId = 1;

    private WorldEnergyNet(final ServerLevel level) {
        this.level = level;
    }

    public static WorldEnergyNet get(final Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            throw new IllegalStateException("WorldEnergyNet is server-only");
        }
        return NETS.computeIfAbsent(level.dimension(), key -> new WorldEnergyNet(serverLevel));
    }

    public static void removeLevel(final ResourceKey<Level> dimension) {
        NETS.remove(dimension);
    }

    public static boolean isEnabled() {
        return ModConfig.GLOBAL_ENERGY_NET_ENABLED.get();
    }

    public void registerCable(final BaseCableBlockEntity cable) {
        if (!isEnabled()) {
            return;
        }
        BlockPos pos = cable.getBlockPos();
        registeredCables.add(pos);
        for (Direction direction : Direction.values()) {
            dissolveGridAt(pos.relative(direction));
        }
        dissolveGridAt(pos);
        rebuildGridAt(pos);
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.relative(direction);
            if (registeredCables.contains(neighbor)) {
                rebuildGridAt(neighbor);
            }
        }
    }

    public void unregisterCable(final BlockPos pos) {
        registeredCables.remove(pos);
        activeCables.remove(pos);
        dissolveGridAt(pos);
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.relative(direction);
            if (registeredCables.contains(neighbor)) {
                rebuildGridAt(neighbor);
            }
        }
    }

    public void markActive(final BlockPos pos) {
        if (registeredCables.contains(pos)) {
            activeCables.add(pos);
        }
    }

    @Override
    public void invalidateGrid(final BlockPos pos) {
        if (!isEnabled()) {
            return;
        }
        dissolveGridAt(pos);
        if (isConductor(level, pos)) {
            rebuildGridAt(pos);
        }
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pos.relative(direction);
            if (registeredCables.contains(neighbor)) {
                EnergyGrid grid = gridByPos.get(neighbor);
                if (grid != null) {
                    grid.invalidateMask();
                }
            }
        }
        EnergyTransferHelper.invalidateCableCluster(level, pos);
    }

    public int getAcceptorMaskForCable(final BlockPos cablePos) {
        EnergyGrid grid = gridByPos.get(cablePos);
        if (grid != null) {
            return grid.getAcceptorMask(level);
        }
        return EnergyTransferHelper.buildDirectNeighborAcceptorMask(level, cablePos);
    }

    public void tick() {
        if (!isEnabled() || activeCables.isEmpty()) {
            return;
        }

        TickProfiler.profileCable(() -> {
            Set<BlockPos> snapshot = new HashSet<>(activeCables);
            activeCables.clear();
            for (BlockPos pos : snapshot) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (!(blockEntity instanceof BaseCableBlockEntity cable) || cable.isBurnedOutForNet()) {
                    continue;
                }
                cable.tickCable();
                if (cable.hasNetActivity()) {
                    activeCables.add(pos);
                }
            }
        });
    }

    @Override
    public int getRegisteredCableCount() {
        return registeredCables.size();
    }

    @Override
    public int getActiveCableCount() {
        return activeCables.size();
    }

    @Override
    public int getGridId(final BlockPos pos) {
        EnergyGrid grid = gridByPos.get(pos);
        return grid == null ? -1 : grid.id();
    }

    private void dissolveGridAt(final BlockPos pos) {
        EnergyGrid grid = gridByPos.remove(pos);
        if (grid == null) {
            return;
        }
        for (BlockPos member : new HashSet<>(grid.members())) {
            gridByPos.remove(member);
        }
        gridsById.remove(grid.id());
    }

    private void rebuildGridAt(final BlockPos start) {
        if (!isConductor(level, start)) {
            return;
        }
        EnergyGrid existing = gridByPos.get(start);
        if (existing != null) {
            existing.invalidateMask();
            return;
        }

        EnergyGrid grid = new EnergyGrid(nextGridId++);
        gridsById.put(grid.id(), grid);

        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (!grid.addMember(current)) {
                continue;
            }
            gridByPos.put(current, grid);
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (!grid.contains(next) && isConductor(level, next)) {
                    queue.add(next);
                }
            }
        }
    }

    static boolean isConductor(final Level level, final BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BaseCableBlockEntity) {
            return true;
        }
        if (blockEntity == null) {
            return false;
        }
        return blockEntity.getCapability(ModCapabilities.ENERGY_NODE_CAPABILITY, null)
                .map(node -> node instanceof IEnergyConductor)
                .orElse(false);
    }

    /**
     * Flood-fills a conductor component from {@code start} using the supplied adjacency predicate.
     * Package-private for unit tests without a loaded level.
     */
    static Set<BlockPos> floodFill(
            final BlockPos start,
            final java.util.function.Predicate<BlockPos> isConductorAt) {
        Set<BlockPos> members = new HashSet<>();
        if (!isConductorAt.test(start)) {
            return members;
        }
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (!members.add(current)) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (!members.contains(next) && isConductorAt.test(next)) {
                    queue.add(next);
                }
            }
        }
        return members;
    }
}
