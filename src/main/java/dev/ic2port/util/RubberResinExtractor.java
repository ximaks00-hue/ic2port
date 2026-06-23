package dev.ic2port.util;

import dev.ic2port.block.RubberWoodBlock;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shared resin extraction logic for manual and electric tree taps.
 */
public final class RubberResinExtractor {

    public static final float MANUAL_OVERTAP_CHANCE = 0.35F;
    public static final float ELECTRIC_OVERTAP_CHANCE = 0.15F;

    @FunctionalInterface
    public interface TapCost {
        /**
         * @return {@code true} if the tool could pay for this tap attempt
         */
        boolean tryPay(ItemStack tool, Player player, InteractionHand hand);
    }

    private RubberResinExtractor() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static InteractionResult tryExtract(
            final BlockState state,
            final Level level,
            final BlockPos pos,
            final Player player,
            final InteractionHand hand,
            final ItemStack tool,
            final TapCost cost,
            final float overtappingChance) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player == null) {
            return InteractionResult.PASS;
        }

        if (state.getValue(RubberWoodBlock.RESIN)) {
            if (!cost.tryPay(tool, player, hand)) {
                return InteractionResult.FAIL;
            }
            ItemStack resin = new ItemStack(ItemRegistry.STICKY_RESIN.get());
            if (!player.getInventory().add(resin)) {
                player.drop(resin, false);
            }
            level.setBlock(pos, state.setValue(RubberWoodBlock.RESIN, false), 3);
            RubberWoodBlock.playTapSound(level, pos);
            return InteractionResult.CONSUME;
        }

        if (state.getValue(RubberWoodBlock.DEPLETED)) {
            player.displayClientMessage(Component.translatable("message.ic2port.tree_tap.depleted"), true);
            return InteractionResult.FAIL;
        }

        if (!cost.tryPay(tool, player, hand)) {
            return InteractionResult.FAIL;
        }

        RubberWoodBlock.playTapSound(level, pos);
        if (level.random.nextFloat() < overtappingChance) {
            level.setBlock(pos, state.setValue(RubberWoodBlock.DEPLETED, true), 3);
            player.displayClientMessage(Component.translatable("message.ic2port.tree_tap.overtapped"), true);
        } else {
            player.displayClientMessage(Component.translatable("message.ic2port.tree_tap.empty"), true);
        }
        return InteractionResult.CONSUME;
    }

    public static TapCost durabilityCost() {
        return (tool, player, hand) -> {
            tool.hurtAndBreak(1, player, broken -> broken.broadcastBreakEvent(hand));
            return true;
        };
    }

    /**
     * Extracts one resin blob for automated collectors such as the sticky tube.
     */
    public static ItemStack extractForAutomation(
            final Level level,
            final BlockPos pos,
            final BlockState state) {
        if (!state.getValue(RubberWoodBlock.RESIN)) {
            return ItemStack.EMPTY;
        }
        level.setBlock(pos, state.setValue(RubberWoodBlock.RESIN, false), 3);
        RubberWoodBlock.playTapSound(level, pos);
        return new ItemStack(ItemRegistry.STICKY_RESIN.get());
    }
}
