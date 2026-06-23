package dev.ic2port.util;

import dev.ic2port.api.energy.IEnergyConductor;
import dev.ic2port.blockentity.BaseCableBlockEntity;
import dev.ic2port.setup.ModCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Chunk-scoped cable clustering for EU net v2 — caches connected conductor groups to reduce
 * per-tick neighbor scans on dense cable grids.
 */
public final class EnergyCableClusterHelper {

  private static final Map<Long, ClusterData> CHUNK_CLUSTERS = new HashMap<>();

  private EnergyCableClusterHelper() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static void invalidateChunk(final Level level, final ChunkPos chunkPos) {
    CHUNK_CLUSTERS.remove(chunkKey(level, chunkPos));
  }

  /**
   * @return cached neighbor acceptor mask for the cluster containing {@code cablePos}
   */
  public static int getClusterAcceptorMask(final Level level, final BlockPos cablePos) {
    ClusterData data = getOrBuild(level, cablePos);
    return data != null ? data.acceptorMask : buildDirectMask(level, cablePos);
  }

    private static ClusterData getOrBuild(final Level level, final BlockPos origin) {
        ChunkPos chunk = new ChunkPos(origin);
        long key = chunkKey(level, chunk);
    ClusterData existing = CHUNK_CLUSTERS.get(key);
    if (existing != null && existing.contains(origin)) {
      return existing;
    }

    Set<BlockPos> cluster = floodFillCables(level, origin);
    if (cluster.isEmpty()) {
      return null;
    }

    int mask = 0;
    for (BlockPos member : cluster) {
      mask |= buildDirectMask(level, member);
    }

    ClusterData data = new ClusterData(cluster, mask);
    CHUNK_CLUSTERS.put(key, data);
    return data;
  }

  private static Set<BlockPos> floodFillCables(final Level level, final BlockPos start) {
    Set<BlockPos> visited = new HashSet<>();
    if (!isCable(level, start)) {
      return visited;
    }

    ChunkPos chunk = new ChunkPos(start);
    Set<BlockPos> queue = new HashSet<>();
    queue.add(start);

    while (!queue.isEmpty()) {
      BlockPos current = queue.iterator().next();
      queue.remove(current);
      if (!visited.add(current)) {
        continue;
      }
      if (!new ChunkPos(current).equals(chunk)) {
        continue;
      }
      for (Direction direction : Direction.values()) {
        BlockPos next = current.relative(direction);
        if (!visited.contains(next) && isCable(level, next)) {
          queue.add(next);
        }
      }
    }
    return visited;
  }

  private static boolean isCable(final Level level, final BlockPos pos) {
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

  private static int buildDirectMask(final Level level, final BlockPos sourcePos) {
    int mask = 0;
    for (Direction direction : Direction.values()) {
      BlockPos targetPos = sourcePos.relative(direction);
      Direction targetFacing = direction.getOpposite();
      if (EnergyTransferHelper.getAcceptor(level, targetPos, targetFacing) != null) {
        mask |= EnergyTransferHelper.faceBit(direction);
      }
    }
    return mask;
  }

  private static long chunkKey(final Level level, final ChunkPos chunkPos) {
    return level.dimension().location().toString().hashCode() * 31L + chunkPos.toLong();
  }

  private static final class ClusterData {
    private final Set<BlockPos> members;
    private final int acceptorMask;

    private ClusterData(final Set<BlockPos> members, final int acceptorMask) {
      this.members = members;
      this.acceptorMask = acceptorMask;
    }

    private boolean contains(final BlockPos pos) {
      return members.contains(pos);
    }
  }
}
