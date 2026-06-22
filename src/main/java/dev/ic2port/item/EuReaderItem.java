package dev.ic2port.item;

import dev.ic2port.api.items.IEUReader;
import dev.ic2port.util.EuReaderFlowService;
import dev.ic2port.util.EuReaderHelper;
import dev.ic2port.util.EuReaderMode;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Reads stored EU, tier limits, cable flow, and averaged EU/t from energy network blocks.
 */
public class EuReaderItem extends Item implements IEUReader {

    private static final int USE_COOLDOWN_TICKS = 10;

    public EuReaderItem(final Properties properties) {
        super(properties.durability(32));
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

        ItemStack stack = context.getItemInHand();
        if (EuReaderMode.fromStack(stack) == EuReaderMode.FLOW) {
            if (EuReaderFlowService.showProgress(player, context.getClickedPos())) {
                return InteractionResult.SUCCESS;
            }
            if (EuReaderFlowService.start(player, level, context.getClickedPos())) {
                finishReading(level, player, context.getHand(), false);
                return InteractionResult.CONSUME;
            }
            player.displayClientMessage(Component.translatable("message.ic2port.eu_reader.no_target"), true);
            return InteractionResult.FAIL;
        }

        if (EuReaderHelper.measureBlock(level, context.getClickedPos(), player)) {
            finishReading(level, player, context.getHand(), true);
            return InteractionResult.CONSUME;
        }

        player.displayClientMessage(Component.translatable("message.ic2port.eu_reader.no_target"), true);
        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        if (player.isShiftKeyDown()) {
            EuReaderMode mode = EuReaderMode.toggleOnStack(stack);
            EuReaderFlowService.cancel(player.getUUID());
            level.playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.UI_BUTTON_CLICK.value(),
                    SoundSource.PLAYERS,
                    0.5F,
                    1.1F);
            player.displayClientMessage(
                    Component.translatable(
                            "message.ic2port.eu_reader.mode",
                            Component.translatable(mode == EuReaderMode.STATS
                                    ? "message.ic2port.eu_reader.mode.stats"
                                    : "message.ic2port.eu_reader.mode.flow")),
                    true);
            return InteractionResultHolder.success(stack);
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(other);

        if (!otherStack.isEmpty() && EuReaderHelper.measureItem(otherStack, player)) {
            finishReading(level, player, hand, true);
            return InteractionResultHolder.success(stack);
        }

        player.displayClientMessage(Component.translatable("message.ic2port.eu_reader.hint"), true);
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ic2port.eu_reader.hint"));
        EuReaderMode mode = EuReaderMode.fromStack(stack);
        tooltip.add(Component.translatable(
                "item.ic2port.eu_reader.mode",
                Component.translatable(mode == EuReaderMode.STATS
                        ? "message.ic2port.eu_reader.mode.stats"
                        : "message.ic2port.eu_reader.mode.flow")));
    }

    private void finishReading(final Level level, final Player player, final InteractionHand hand, final boolean wearDurability) {
        player.getCooldowns().addCooldown(this, USE_COOLDOWN_TICKS);
        level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.UI_BUTTON_CLICK.value(),
                SoundSource.PLAYERS,
                0.5F,
                1.0F);
        if (wearDurability) {
            player.getItemInHand(hand).hurtAndBreak(1, player, broken -> broken.broadcastBreakEvent(hand));
        }
    }
}
