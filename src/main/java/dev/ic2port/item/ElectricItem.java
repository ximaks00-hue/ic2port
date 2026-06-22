package dev.ic2port.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Base class for IC2 items that store EU in their {@link ItemStack} NBT.
 */
public abstract class ElectricItem extends Item implements IElectricItem {

    private static final String ENERGY_TAG = "Energy";

    private final double maxEnergy;
    private final int tier;

    protected ElectricItem(final Properties properties, final double maxEnergy, final int tier) {
        super(properties);
        this.maxEnergy = maxEnergy;
        this.tier = tier;
    }

    public double getMaxEnergy() {
        return maxEnergy;
    }

    public int getTier() {
        return tier;
    }

    public double getStoredEnergy(final ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 0.0D;
        }
        return Math.min(maxEnergy, Math.max(0.0D, tag.getDouble(ENERGY_TAG)));
    }

    public void setStoredEnergy(final ItemStack stack, final double energy) {
        stack.getOrCreateTag().putDouble(ENERGY_TAG, Math.max(0.0D, Math.min(maxEnergy, energy)));
    }

    /**
     * @return remainder that could not be accepted
     */
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

    /**
     * @return EU actually extracted from the stack
     */
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
        return net.minecraft.util.Mth.hsvToRgb(0.33F, 1.0F, 0.5F + ratio * 0.5F);
    }
}
