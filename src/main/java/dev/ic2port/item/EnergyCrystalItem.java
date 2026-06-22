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
 * Portable MV energy storage — rechargeable at MFE tier and above.
 */
public class EnergyCrystalItem extends ElectricItem {

    public static final double CAPACITY = 100_000.0D;

    public EnergyCrystalItem(final Properties properties) {
        super(properties.stacksTo(1), CAPACITY, EnergyTier.MV);
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        tooltip.add(Component.translatable(
                        "item.ic2port.energy_crystal.energy",
                        (int) Math.round(getStoredEnergy(stack)),
                        (int) Math.round(getMaxEnergy()))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.ic2port.energy_crystal.tier_hint")
                .withStyle(ChatFormatting.BLUE));
    }
}
