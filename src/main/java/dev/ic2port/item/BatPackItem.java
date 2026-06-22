package dev.ic2port.item;

import dev.ic2port.api.energy.EnergyTier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Wearable LV EU storage that trickle-charges held electric tools.
 */
public class BatPackItem extends ElectricArmorItem implements IPortableEnergyPack {

    public static final double CAPACITY = 60_000.0D;
    public static final double CHARGE_PER_TICK = EnergyTier.LV_MAX_PACKET;

    public BatPackItem(final Properties properties) {
        super(ArmorMaterials.IRON, Type.CHESTPLATE, properties, CAPACITY, EnergyTier.LV);
    }

    @Override
    public double getChargePerTick() {
        return CHARGE_PER_TICK;
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        tooltip.add(Component.translatable(
                        "item.ic2port.batpack.energy",
                        (int) Math.round(getStoredEnergy(stack)),
                        (int) Math.round(getMaxEnergy()))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.ic2port.batpack.charge_hint")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
