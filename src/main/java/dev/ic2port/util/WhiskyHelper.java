package dev.ic2port.util;

import dev.ic2port.item.WhiskyItem;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * NBT helpers for barrel-aged whisky.
 */
public final class WhiskyHelper {

    public static final String TAG_YEARS = "WhiskyYears";

    public static final int GRIST_COST = 16;
    public static final int YEAR_TICKS = 1200;
    public static final int MAX_YEARS = 50;
    public static final int BREW_DURATION = YEAR_TICKS * MAX_YEARS;

    private WhiskyHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ItemStack createWhisky(final int years) {
        int age = Math.max(1, Math.min(MAX_YEARS, years));
        ItemStack stack = new ItemStack(ItemRegistry.WHISKY.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_YEARS, age);
        if (age >= 40) {
            stack.setHoverName(Component.translatable("item.ic2port.whisky.aged", age));
        } else if (age <= 5) {
            stack.setHoverName(Component.translatable("item.ic2port.whisky.young"));
        }
        return stack;
    }

    public static int getYears(final ItemStack stack) {
        if (!(stack.getItem() instanceof WhiskyItem)) {
            return 1;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_YEARS)) {
            return 1;
        }
        return tag.getInt(TAG_YEARS);
    }

    public static int yearsFromProgress(final int brewProgress) {
        return Math.max(1, Math.min(MAX_YEARS, brewProgress / YEAR_TICKS));
    }
}
