package dev.ic2port.item;

import dev.ic2port.util.ArmorModuleHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Base for items installable in nano / quantum chestplate module slots.
 */
public abstract class ArmorModuleItem extends Item {

    protected ArmorModuleItem(final Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }

        if (player.isShiftKeyDown()) {
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            if (ArmorModuleHelper.tryInstall(chestplate, stack)) {
                if (!level.isClientSide) {
                    stack.shrink(1);
                    player.setItemSlot(EquipmentSlot.CHEST, chestplate);
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
        }

        return onModuleUse(level, player, stack);
    }

    protected InteractionResultHolder<ItemStack> onModuleUse(
            final Level level,
            final Player player,
            final ItemStack stack) {
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ic2port.armor_module.install_hint"));
    }
}
