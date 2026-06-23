package dev.ic2port.item;

import dev.ic2port.api.events.ScrapBoxEvent;
import dev.ic2port.util.ScrapBoxDrops;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Opens into a random reward when used — classic IC2 scrap box.
 */
public class ScrapBoxItem extends Item {

    public ScrapBoxItem(final Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        stack.shrink(1);
        ItemStack reward = ScrapBoxDrops.roll(level.random);
        ScrapBoxEvent event = ScrapBoxEvent.onOpen(player, stack, reward);
        if (event == null) {
            return InteractionResultHolder.consume(stack);
        }
        reward = event.getReward();
        if (!player.getInventory().add(reward)) {
            player.drop(reward, false);
        }

        level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.ITEM_PICKUP,
                SoundSource.PLAYERS,
                0.8F,
                0.9F + level.random.nextFloat() * 0.2F);

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ic2port.scrap_box.hint")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.ic2port.scrap_box.loot_hint")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
