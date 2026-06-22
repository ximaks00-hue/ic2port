package dev.ic2port.item;

import dev.ic2port.api.items.IThermometer;
import dev.ic2port.util.ThermometerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Reads hull heat from reactors, rotor heat from thermal centrifuges, or component heat from held parts.
 */
public class ThermometerItem extends Item implements IThermometer {

    private static final int USE_COOLDOWN_TICKS = 10;

    public ThermometerItem(final Properties properties) {
        super(properties.durability(16));
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null || level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResult.FAIL;
        }

        if (ThermometerHelper.measureBlock(level, context.getClickedPos(), player)) {
            finishReading(level, player, context.getHand());
            return InteractionResult.CONSUME;
        }

        player.displayClientMessage(Component.translatable("message.ic2port.thermometer.no_target"), true);
        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(other);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!otherStack.isEmpty() && ThermometerHelper.measureComponent(otherStack, player)) {
            finishReading(level, player, hand);
            return InteractionResultHolder.success(stack);
        }

        player.displayClientMessage(Component.translatable("message.ic2port.thermometer.hint"), true);
        return InteractionResultHolder.fail(stack);
    }

    private void finishReading(final Level level, final Player player, final InteractionHand hand) {
        player.getCooldowns().addCooldown(this, USE_COOLDOWN_TICKS);
        level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.UI_BUTTON_CLICK.value(),
                SoundSource.PLAYERS,
                0.5F,
                1.0F);
        player.getItemInHand(hand).hurtAndBreak(1, player, broken -> broken.broadcastBreakEvent(hand));
    }
}
