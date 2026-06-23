package dev.ic2port.item;

import dev.ic2port.util.TubeConfiguratorHelper;
import dev.ic2port.util.TubeConfiguratorHelper.ConfigMode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * IC2-style tube configurator — enables extra extraction sides, output blocking and redstone gating.
 */
public class TubeConfiguratorItem extends Item {

    public TubeConfiguratorItem(final Properties properties) {
        super(properties.durability(128));
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());
        ItemStack stack = context.getItemInHand();
        ConfigMode mode = ConfigMode.fromStack(stack);
        if (TubeConfiguratorHelper.apply(
                mode,
                level.getBlockState(context.getClickedPos()),
                blockEntity,
                context.getClickedFace(),
                player)) {
            stack.hurtAndBreak(1, player, user -> user.broadcastBreakEvent(context.getHand()));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        ConfigMode next = ConfigMode.fromStack(stack).next();
        ConfigMode.writeToStack(stack, next);
        player.displayClientMessage(Component.translatable(
                "message.ic2port.tube.config_mode",
                Component.translatable("message.ic2port.tube.config_mode." + next.translationKey())), true);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean isFoil(final ItemStack stack) {
        return true;
    }
}
