package dev.ic2port.item;

import dev.ic2port.block.RubberWoodBlock;
import dev.ic2port.util.RubberTreeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Strips an entire rubber tree for bulk sticky resin (IC2 advanced tree tap).
 */
public class AdvancedTreeTapItem extends Item {

    public static final int DURABILITY_PER_TREE = 3;

    public AdvancedTreeTapItem(final Properties properties) {
        super(properties.durability(48));
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof RubberWoodBlock) && !state.is(dev.ic2port.setup.BlockRegistry.RUBBER_LEAVES.get())) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        if (player != null && player.blockActionRestricted(level, pos, GameType.SURVIVAL)) {
            return InteractionResult.FAIL;
        }
        return RubberTreeHelper.stripTree(
                level,
                pos,
                player,
                context.getHand(),
                context.getItemInHand(),
                DURABILITY_PER_TREE);
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ic2port.advanced_tree_tap.hint"));
    }
}
