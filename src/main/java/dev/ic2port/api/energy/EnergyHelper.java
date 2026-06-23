package dev.ic2port.api.energy;

import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.util.EnergyTransferHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Stable wrapper around internal EU transfer helpers for addon mods.
 */
public final class EnergyHelper {

  private EnergyHelper() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * @param querySide side of the <em>target</em> block facing the source
   */
  @Nullable
  public static IEnergyAcceptor getAcceptor(
      final Level level,
      final BlockPos targetPos,
      final Direction querySide) {
    return EnergyTransferHelper.getAcceptor(level, targetPos, querySide);
  }

  /**
   * @return remainder EU not accepted by the neighbor
   */
  public static double injectIntoNeighbor(
      final Level level,
      final BlockPos sourcePos,
      final Direction outgoingDirection,
      final double amount,
      final int tier) {
    return EnergyTransferHelper.injectIntoNeighbor(level, sourcePos, outgoingDirection, amount, tier);
  }

  /**
   * @return {@link IEnergyNode} exposed on the given face, or {@code null}
   */
  @Nullable
  public static IEnergyNode getEnergyNode(
      final Level level,
      final BlockPos pos,
      @Nullable final Direction side) {
    BlockEntity blockEntity = level.getBlockEntity(pos);
    if (blockEntity == null) {
      return null;
    }
    return blockEntity.getCapability(ModCapabilities.ENERGY_NODE_CAPABILITY, side).orElse(null);
  }

  /**
   * Invalidates cached cable cluster data for the chunk containing {@code pos}.
   */
  public static void invalidateCableCluster(final Level level, final BlockPos pos) {
    EnergyTransferHelper.invalidateCableCluster(level, pos);
  }
}
