package dev.ic2port.util;

import net.minecraft.world.item.ItemStack;

/**
 * NBT-backed heat storage for in-reactor components.
 */
public final class ReactorComponentHeat {

    public static final String HEAT_TAG = "ComponentHeat";

    private ReactorComponentHeat() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static double getHeat(final ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(HEAT_TAG)) {
            return 0.0D;
        }
        return stack.getTag().getDouble(HEAT_TAG);
    }

    public static void setHeat(final ItemStack stack, final double heat) {
        if (heat <= 0.0D) {
            if (stack.hasTag()) {
                stack.getTag().remove(HEAT_TAG);
                if (stack.getTag().isEmpty()) {
                    stack.setTag(null);
                }
            }
            return;
        }
        stack.getOrCreateTag().putDouble(HEAT_TAG, heat);
    }

    public static void addHeat(final ItemStack stack, final double delta, final double maxHeat) {
        setHeat(stack, Math.max(0.0D, Math.min(maxHeat, getHeat(stack) + delta)));
    }

    public static boolean isOverloaded(final ItemStack stack, final double maxHeat) {
        return getHeat(stack) >= maxHeat;
    }
}
