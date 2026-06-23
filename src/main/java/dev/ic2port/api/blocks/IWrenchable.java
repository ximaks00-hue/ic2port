package dev.ic2port.api.blocks;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Blocks that support IC2 wrench dismantling semantics.
 * <p>
 * Implementations may override drop chance and pre-dismantle hooks.
 */
public interface IWrenchable {

  /**
   * @return {@code true} if the wrench may dismantle this block in the given state
   */
  default boolean canWrench(final Player player, final BlockState state) {
    return true;
  }

  /**
   * @return drop chance in {@code [0, 1]} when dismantled with a wrench
   */
  default double getWrenchDropChance(final Player player, final BlockState state, final boolean sneaking) {
    return sneaking ? 0.95D : 0.8D;
  }

  /**
   * Called before the block is removed; return {@code false} to abort dismantling.
   */
  default boolean onWrenchRemove(final UseOnContext context, final BlockState state) {
    return true;
  }
}
