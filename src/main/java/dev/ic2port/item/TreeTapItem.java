package dev.ic2port.item;

import dev.ic2port.block.RubberWoodBlock;
import dev.ic2port.util.RubberResinExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Extracts sticky resin from {@link RubberWoodBlock} resin spots.
 */
public class TreeTapItem extends Item {

    public TreeTapItem(final Properties properties) {
        super(properties.durability(16));
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof RubberWoodBlock)) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        if (player != null && player.blockActionRestricted(level, pos, GameType.SURVIVAL)) {
            return InteractionResult.FAIL;
        }
        return RubberResinExtractor.tryExtract(
                state,
                level,
                pos,
                player,
                context.getHand(),
                context.getItemInHand(),
                RubberResinExtractor.durabilityCost(),
                RubberResinExtractor.MANUAL_OVERTAP_CHANCE);
    }
}
