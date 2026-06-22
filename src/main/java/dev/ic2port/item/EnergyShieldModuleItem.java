package dev.ic2port.item;

import dev.ic2port.util.ModuleEnergyHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Extra EU shield layer installable in nano / quantum chestplate module slots.
 */
public class EnergyShieldModuleItem extends ArmorModuleItem {

    public static final double CAPACITY = 100_000.0D;
    public static final double EU_PER_DAMAGE = 300.0D;
    public static final double EU_PER_FALL_DAMAGE = 150.0D;

    public EnergyShieldModuleItem(final Properties properties) {
        super(properties);
    }

    public double getStoredEnergy(final ItemStack stack) {
        return ModuleEnergyHelper.getStoredEnergy(stack, CAPACITY);
    }

    public static float absorbDamage(final ItemStack moduleStack, final float damage, final boolean fallDamage) {
        if (damage <= 0.0F || !(moduleStack.getItem() instanceof EnergyShieldModuleItem)) {
            return damage;
        }
        double euPerDamage = fallDamage ? EU_PER_FALL_DAMAGE : EU_PER_DAMAGE;
        double energyNeeded = damage * euPerDamage;
        double drawn = ModuleEnergyHelper.drawEnergy(moduleStack, CAPACITY, energyNeeded);
        float absorbed = (float) (drawn / euPerDamage);
        return Math.max(0.0F, damage - absorbed);
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        int stored = (int) Math.round(getStoredEnergy(stack));
        tooltip.add(Component.translatable("item.ic2port.energy_shield_module.energy", stored, (int) CAPACITY)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.ic2port.energy_shield_module.hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
