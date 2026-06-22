package dev.ic2port.util;

import dev.ic2port.block.RubberWoodBlock;
import dev.ic2port.setup.BlockRegistry;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Finds connected rubber trees and strips them for sticky resin (advanced tree tap).
 */
public final class RubberTreeHelper {

    private static final int MAX_TREE_BLOCKS = 128;

    private RubberTreeHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static InteractionResult stripTree(
            final Level level,
            final BlockPos start,
            final Player player,
            final InteractionHand hand,
            final ItemStack tool,
            final int durabilityCost) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Set<BlockPos> tree = findConnectedTree(level, start);
        if (tree.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.ic2port.advanced_tree_tap.no_tree"), true);
            return InteractionResult.FAIL;
        }

        boolean hasLeaves = false;
        int resin = 0;
        for (BlockPos pos : tree) {
            BlockState state = level.getBlockState(pos);
            if (state.is(BlockRegistry.RUBBER_LEAVES.get())) {
                hasLeaves = true;
            } else if (state.is(BlockRegistry.RUBBER_WOOD.get())) {
                if (state.getValue(RubberWoodBlock.RESIN)) {
                    resin++;
                } else if (level.random.nextFloat() < 0.2F) {
                    resin++;
                }
            }
        }

        if (!hasLeaves) {
            player.displayClientMessage(Component.translatable("message.ic2port.advanced_tree_tap.no_tree"), true);
            return InteractionResult.FAIL;
        }

        tool.hurtAndBreak(durabilityCost, player, broken -> broken.broadcastBreakEvent(hand));

        List<BlockPos> ordered = new ArrayList<>(tree);
        ordered.sort((a, b) -> Integer.compare(b.getY(), a.getY()));
        for (BlockPos pos : ordered) {
            level.destroyBlock(pos, true, player);
        }

        if (resin > 0) {
            ItemStack resinStack = new ItemStack(ItemRegistry.STICKY_RESIN.get(), resin);
            if (!player.getInventory().add(resinStack)) {
                player.drop(resinStack, false);
            }
        }

        RubberWoodBlock.playTapSound(level, start);
        player.displayClientMessage(
                Component.translatable("message.ic2port.advanced_tree_tap.result", resin, tree.size()),
                true);
        return InteractionResult.CONSUME;
    }

    private static Set<BlockPos> findConnectedTree(final Level level, final BlockPos start) {
        BlockState startState = level.getBlockState(start);
        if (!startState.is(BlockRegistry.RUBBER_WOOD.get()) && !startState.is(BlockRegistry.RUBBER_LEAVES.get())) {
            return Set.of();
        }

        Set<BlockPos> found = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        found.add(start);

        while (!queue.isEmpty() && found.size() < MAX_TREE_BLOCKS) {
            BlockPos current = queue.poll();
            for (BlockPos next : BlockPos.betweenClosed(
                    current.offset(-1, -1, -1),
                    current.offset(1, 1, 1))) {
                BlockPos immutable = next.immutable();
                if (!found.add(immutable)) {
                    continue;
                }
                BlockState state = level.getBlockState(immutable);
                if (state.is(BlockRegistry.RUBBER_WOOD.get()) || state.is(BlockRegistry.RUBBER_LEAVES.get())) {
                    queue.add(immutable);
                } else {
                    found.remove(immutable);
                }
            }
        }
        return found;
    }
}
