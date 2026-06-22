package dev.ic2port.item;

import dev.ic2port.api.energy.EnergyTier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Portable HV energy storage — rechargeable at MFSU tier.
 */
public class LapotronCrystalItem extends ElectricItem {

    public static final double CAPACITY = 1_000_000.0D;

    public LapotronCrystalItem(final Properties properties) {
        super(properties.stacksTo(1), CAPACITY, EnergyTier.HV);
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        tooltip.add(Component.translatable(
                        "item.ic2port.lapotron_crystal.energy",
                        (int) Math.round(getStoredEnergy(stack)),
                        (int) Math.round(getMaxEnergy()))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.ic2port.lapotron_crystal.tier_hint")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
