package dev.ic2port.util;

import dev.ic2port.item.RumItem;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * NBT helpers for barrel-brewed rum.
 */
public final class RumHelper {

    public static final String TAG_STRENGTH = "RumStrength";

    public static final int SUGAR_CANE_COST = 32;
    public static final int BREW_DURATION = 3600;

    private RumHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ItemStack createRum(final int sugarCaneUsed) {
        int strength = calculateStrength(sugarCaneUsed);
        ItemStack stack = new ItemStack(ItemRegistry.RUM.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_STRENGTH, strength);
        if (strength >= 4) {
            stack.setHoverName(Component.translatable("item.ic2port.rum.strong"));
        } else if (strength <= 2) {
            stack.setHoverName(Component.translatable("item.ic2port.rum.weak"));
        }
        return stack;
    }

    public static int getStrength(final ItemStack stack) {
        if (!(stack.getItem() instanceof RumItem)) {
            return 3;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_STRENGTH)) {
            return 3;
        }
        return tag.getInt(TAG_STRENGTH);
    }

    public static boolean isRum(final ItemStack stack) {
        return stack.getItem() instanceof RumItem && stack.getTag() != null && stack.getTag().contains(TAG_STRENGTH);
    }

    private static int calculateStrength(final int sugarCane) {
        if (sugarCane < SUGAR_CANE_COST / 2) {
            return 1;
        }
        if (sugarCane < SUGAR_CANE_COST) {
            return 2;
        }
        if (sugarCane == SUGAR_CANE_COST) {
            return 4;
        }
        return Math.min(5, 3 + (sugarCane - SUGAR_CANE_COST) / 16);
    }
}
