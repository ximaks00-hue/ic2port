package dev.ic2port.item;

import dev.ic2port.util.TransportUpgradeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
 * Base class for import/export transport upgrades with filter and facing NBT.
 */
public abstract class TransportUpgradeItem extends Item implements ITransportUpgrade {

    private final String tooltipKey;

    protected TransportUpgradeItem(final Properties properties, final String tooltipKey) {
        super(properties);
        this.tooltipKey = tooltipKey;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            final Level level,
            final Player player,
            final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty()) {
            TransportUpgradeHelper.setFilter(stack, offhand);
            player.displayClientMessage(
                    Component.translatable("item.ic2port.transport_upgrade.filter_set", offhand.getHoverName()),
                    true);
        } else {
            TransportUpgradeHelper.cycleSide(stack);
            player.displayClientMessage(TransportUpgradeHelper.sideTooltip(stack), true);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        tooltip.add(Component.translatable(tooltipKey).withStyle(ChatFormatting.GRAY));
        ItemStack filter = TransportUpgradeHelper.getFilter(stack);
        if (!filter.isEmpty()) {
            tooltip.add(Component.translatable("item.ic2port.transport_upgrade.filter", filter.getHoverName())
                    .withStyle(ChatFormatting.DARK_AQUA));
        }
        tooltip.add(Component.literal("").append(TransportUpgradeHelper.sideTooltip(stack))
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.ic2port.transport_upgrade.configure")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
