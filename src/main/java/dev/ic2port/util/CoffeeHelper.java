package dev.ic2port.util;

import dev.ic2port.item.CoffeeItem;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * NBT helpers for barrel-brewed coffee.
 */
public final class CoffeeHelper {

    public static final String TAG_STRENGTH = "CoffeeStrength";

    public static final int BEAN_COST = 4;
    public static final int BREW_DURATION = 3000;

    private CoffeeHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ItemStack createCoffee(final int beansUsed) {
        int strength = calculateStrength(beansUsed);
        ItemStack stack = new ItemStack(ItemRegistry.COFFEE.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_STRENGTH, strength);
        if (strength >= 4) {
            stack.setHoverName(Component.translatable("item.ic2port.coffee.strong"));
        } else if (strength <= 2) {
            stack.setHoverName(Component.translatable("item.ic2port.coffee.weak"));
        }
        return stack;
    }

    public static int getStrength(final ItemStack stack) {
        if (!(stack.getItem() instanceof CoffeeItem)) {
            return 3;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_STRENGTH)) {
            return 3;
        }
        return tag.getInt(TAG_STRENGTH);
    }

    private static int calculateStrength(final int beans) {
        if (beans < BEAN_COST / 2) {
            return 1;
        }
        if (beans < BEAN_COST) {
            return 2;
        }
        if (beans == BEAN_COST) {
            return 4;
        }
        return Math.min(5, 3 + (beans - BEAN_COST) / 4);
    }
}
