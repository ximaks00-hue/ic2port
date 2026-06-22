package dev.ic2port.item;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.util.ArmorSetHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Nano armor — MV electric suit between hazmat and quantum. Absorbs damage with EU, no flight.
 */
public class NanoSuitItem extends ElectricArmorItem {

    public static final double EU_PER_DAMAGE = 400.0D;
    public static final double EU_PER_FALL_DAMAGE = 200.0D;
    public static final double PIECE_CAPACITY = 100_000.0D;

    public NanoSuitItem(final Type type, final Properties properties) {
        super(ArmorMaterials.DIAMOND, type, properties, PIECE_CAPACITY, EnergyTier.MV);
    }

    public static boolean hasFullSet(final Player player) {
        return ArmorSetHelper.hasFullTypedSet(player, NanoSuitItem.class);
    }

    public static float absorbDamage(final Player player, final float damage) {
        return absorbDamage(player, damage, EU_PER_DAMAGE, null);
    }

    public static float absorbFallDamage(final Player player, final float damage) {
        return absorbDamage(player, damage, EU_PER_FALL_DAMAGE, EquipmentSlot.FEET);
    }

    private static float absorbDamage(
            final Player player,
            final float damage,
            final double euPerDamage,
            final @Nullable EquipmentSlot preferredSlot) {
        if (damage <= 0.0F || euPerDamage <= 0.0D) {
            return damage;
        }

        double energyNeeded = damage * euPerDamage;
        double energyUsed = 0.0D;

        if (preferredSlot != null) {
            energyUsed += drainEnergyFromSlot(player, preferredSlot, energyNeeded);
            energyNeeded -= energyUsed;
        }

        if (energyNeeded > 0.0D) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() != EquipmentSlot.Type.ARMOR || slot == preferredSlot) {
                    continue;
                }
                double drained = drainEnergyFromSlot(player, slot, energyNeeded);
                energyUsed += drained;
                energyNeeded -= drained;
                if (energyNeeded <= 0.0D) {
                    break;
                }
            }
        }

        float absorbedHearts = (float) (energyUsed / euPerDamage);
        return Math.max(0.0F, damage - absorbedHearts);
    }

    private static double drainEnergyFromSlot(final Player player, final EquipmentSlot slot, final double amount) {
        ItemStack stack = player.getItemBySlot(slot);
        if (!(stack.getItem() instanceof NanoSuitItem nano)) {
            return 0.0D;
        }
        return nano.drawEnergy(stack, amount);
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.ic2port.nano_suit.absorb_hint", (int) EU_PER_DAMAGE)
                .withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.translatable("item.ic2port.nano_suit.abilities_hint")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
