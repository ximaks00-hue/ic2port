package dev.ic2port.item;

import dev.ic2port.api.energy.EnergyTier;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Base class for IC2 armor pieces that store EU in their {@link ItemStack} NBT.
 */
public abstract class ElectricArmorItem extends ArmorItem implements IElectricItem {

    private static final String ENERGY_TAG = "Energy";

    private final double maxEnergy;
    private final int tier;

    protected ElectricArmorItem(
            final ArmorMaterial material,
            final Type type,
            final Properties properties,
            final double maxEnergy,
            final int tier) {
        super(material, type, properties.stacksTo(1));
        this.maxEnergy = maxEnergy;
        this.tier = tier;
    }

    @Override
    public double getMaxEnergy() {
        return maxEnergy;
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public double getStoredEnergy(final ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 0.0D;
        }
        return Math.min(maxEnergy, Math.max(0.0D, tag.getDouble(ENERGY_TAG)));
    }

    @Override
    public void setStoredEnergy(final ItemStack stack, final double energy) {
        stack.getOrCreateTag().putDouble(ENERGY_TAG, Math.max(0.0D, Math.min(maxEnergy, energy)));
    }

    @Override
    public double charge(final ItemStack stack, final double amount) {
        if (amount <= 0.0D) {
            return 0.0D;
        }
        double space = maxEnergy - getStoredEnergy(stack);
        double accepted = Math.min(amount, space);
        if (accepted > 0.0D) {
            setStoredEnergy(stack, getStoredEnergy(stack) + accepted);
        }
        return amount - accepted;
    }

    @Override
    public double drawEnergy(final ItemStack stack, final double amount) {
        if (amount <= 0.0D) {
            return 0.0D;
        }
        double stored = getStoredEnergy(stack);
        double drawn = Math.min(amount, stored);
        if (drawn > 0.0D) {
            setStoredEnergy(stack, stored - drawn);
        }
        return drawn;
    }

    @Override
    public boolean isDamageable(final ItemStack stack) {
        return false;
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public boolean isValidRepairItem(final ItemStack stack, final ItemStack repairCandidate) {
        return false;
    }

    @Override
    public boolean isBarVisible(final ItemStack stack) {
        return getStoredEnergy(stack) > 0.0D;
    }

    @Override
    public int getBarWidth(final ItemStack stack) {
        if (maxEnergy <= 0.0D) {
            return 0;
        }
        return Math.round(13.0F * (float) (getStoredEnergy(stack) / maxEnergy));
    }

    @Override
    public int getBarColor(final ItemStack stack) {
        float ratio = maxEnergy > 0.0D ? (float) (getStoredEnergy(stack) / maxEnergy) : 0.0F;
        return Mth.hsvToRgb(0.33F, 1.0F, 0.5F + ratio * 0.5F);
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        tooltip.add(Component.translatable(
                        "item.ic2port.electric_armor.energy",
                        (int) Math.round(getStoredEnergy(stack)),
                        (int) Math.round(maxEnergy))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(rechargeHintKey(tier)).withStyle(ChatFormatting.DARK_AQUA));
    }

    private static String rechargeHintKey(final int tier) {
        return switch (tier) {
            case EnergyTier.MV -> "item.ic2port.electric_armor.recharge.mv";
            case EnergyTier.HV -> "item.ic2port.electric_armor.recharge.hv";
            case EnergyTier.EV -> "item.ic2port.electric_armor.recharge.ev";
            default -> "item.ic2port.electric_armor.recharge.lv";
        };
    }
}
