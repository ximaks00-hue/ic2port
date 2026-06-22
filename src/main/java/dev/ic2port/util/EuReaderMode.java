package dev.ic2port.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * EU Reader display modes.
 */
public enum EuReaderMode {

    STATS,
    FLOW;

    private static final String TAG = "EuReaderMode";

    public static EuReaderMode fromStack(final ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG)) {
            return STATS;
        }
        return FLOW.name().equalsIgnoreCase(tag.getString(TAG)) ? FLOW : STATS;
    }

    public static void writeToStack(final ItemStack stack, final EuReaderMode mode) {
        stack.getOrCreateTag().putString(TAG, mode.name().toLowerCase());
    }

    public static EuReaderMode toggleOnStack(final ItemStack stack) {
        EuReaderMode next = fromStack(stack) == STATS ? FLOW : STATS;
        writeToStack(stack, next);
        return next;
    }
}
