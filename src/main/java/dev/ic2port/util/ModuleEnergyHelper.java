package dev.ic2port.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * EU storage helpers for armor module items (same NBT tag as {@link dev.ic2port.item.IElectricItem}).
 */
public final class ModuleEnergyHelper {

    private static final String ENERGY_TAG = "Energy";

    private ModuleEnergyHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static double getStoredEnergy(final ItemStack stack, final double capacity) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 0.0D;
        }
        return Math.min(capacity, Math.max(0.0D, tag.getDouble(ENERGY_TAG)));
    }

    public static void setStoredEnergy(final ItemStack stack, final double capacity, final double energy) {
        stack.getOrCreateTag().putDouble(ENERGY_TAG, Math.max(0.0D, Math.min(capacity, energy)));
    }

    public static double drawEnergy(final ItemStack stack, final double capacity, final double amount) {
        if (amount <= 0.0D) {
            return 0.0D;
        }
        double stored = getStoredEnergy(stack, capacity);
        double drawn = Math.min(amount, stored);
        if (drawn > 0.0D) {
            setStoredEnergy(stack, capacity, stored - drawn);
        }
        return drawn;
    }
}
