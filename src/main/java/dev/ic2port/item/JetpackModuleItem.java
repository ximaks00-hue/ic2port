package dev.ic2port.item;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.util.ModuleEnergyHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Compact jetpack installable in nano / quantum chestplate module slots.
 */
public class JetpackModuleItem extends ArmorModuleItem {

    public static final double CAPACITY = 30_000.0D;
    public static final double MIN_ACTIVE_ENERGY = 50.0D;
    public static final String JETPACK_MODE_TAG = "JetpackMode";

    public JetpackModuleItem(final Properties properties) {
        super(properties);
    }

    public ElectricJetpackItem.JetpackMode getMode(final ItemStack stack) {
        return ElectricJetpackItem.JetpackMode.fromId(stack.getOrCreateTag().getInt(JETPACK_MODE_TAG));
    }

    public void setMode(final ItemStack stack, final ElectricJetpackItem.JetpackMode mode) {
        stack.getOrCreateTag().putInt(JETPACK_MODE_TAG, mode.ordinal());
    }

    public double getStoredEnergy(final ItemStack stack) {
        return ModuleEnergyHelper.getStoredEnergy(stack, CAPACITY);
    }

    @Override
    protected InteractionResultHolder<ItemStack> onModuleUse(
            final Level level,
            final Player player,
            final ItemStack stack) {
        if (!level.isClientSide) {
            ElectricJetpackItem.JetpackMode next = getMode(stack).next();
            setMode(stack, next);
            player.displayClientMessage(
                    Component.empty()
                            .append(Component.translatable("item.ic2port.electric_jetpack.mode_switch_prefix")
                                    .withStyle(ChatFormatting.GRAY))
                            .append(Component.translatable(next.getTranslationKey()).withStyle(next.getChatColor())),
                    true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        int stored = (int) Math.round(getStoredEnergy(stack));
        tooltip.add(Component.translatable("item.ic2port.electric_jetpack.energy", stored, (int) CAPACITY)
                .withStyle(ChatFormatting.GRAY));
        ElectricJetpackItem.JetpackMode mode = getMode(stack);
        tooltip.add(Component.translatable(
                        "item.ic2port.electric_jetpack.mode",
                        Component.translatable(mode.getTranslationKey()))
                .withStyle(mode.getChatColor()));
        tooltip.add(Component.translatable("item.ic2port.jetpack_module.air_hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
