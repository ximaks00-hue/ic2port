package dev.ic2port.item;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.util.ModuleEnergyHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * MV lapotron-style portable EU storage for nano / quantum chestplate module slots.
 */
public class LapotronEnergyPackModuleItem extends ArmorModuleItem implements IModulePortableEnergyPack {

    public static final double CAPACITY = 300_000.0D;
    public static final double CHARGE_PER_TICK = EnergyTier.MV_MAX_PACKET;
    public static final int TIER = EnergyTier.MV;

    public LapotronEnergyPackModuleItem(final Properties properties) {
        super(properties);
    }

    @Override
    public double getChargePerTick() {
        return CHARGE_PER_TICK;
    }

    @Override
    public double getModuleCapacity() {
        return CAPACITY;
    }

    @Override
    public int getModuleTier() {
        return TIER;
    }

    @Override
    public double getModuleStoredEnergy(final ItemStack stack) {
        return getStoredEnergy(stack);
    }

    @Override
    public double drawModuleEnergy(final ItemStack stack, final double amount) {
        return drawEnergy(stack, amount);
    }

    public double getStoredEnergy(final ItemStack stack) {
        return ModuleEnergyHelper.getStoredEnergy(stack, CAPACITY);
    }

    public double drawEnergy(final ItemStack stack, final double amount) {
        return ModuleEnergyHelper.drawEnergy(stack, CAPACITY, amount);
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        int stored = (int) Math.round(getStoredEnergy(stack));
        tooltip.add(Component.translatable("item.ic2port.lapotron_energy_pack_module.energy", stored, (int) CAPACITY)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.ic2port.lapotron_energy_pack_module.hint")
                .withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("item.ic2port.lappack.tier_hint")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
