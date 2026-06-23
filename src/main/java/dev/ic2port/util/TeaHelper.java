package dev.ic2port.util;

import dev.ic2port.item.TeaItem;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * NBT helpers for barrel-brewed tea.
 */
public final class TeaHelper {

    public static final String TAG_QUALITY = "TeaQuality";

    public static final int TEA_LEAF_COST = 4;
    public static final int BREW_DURATION = 2400;

    private TeaHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ItemStack createTea(final int leavesUsed) {
        int quality = calculateQuality(leavesUsed);
        ItemStack stack = new ItemStack(ItemRegistry.TEA.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_QUALITY, quality);
        if (quality >= 4) {
            stack.setHoverName(Component.translatable("item.ic2port.tea.strong"));
        } else if (quality <= 2) {
            stack.setHoverName(Component.translatable("item.ic2port.tea.weak"));
        }
        return stack;
    }

    public static int getQuality(final ItemStack stack) {
        if (!(stack.getItem() instanceof TeaItem)) {
            return 3;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_QUALITY)) {
            return 3;
        }
        return tag.getInt(TAG_QUALITY);
    }

    private static int calculateQuality(final int leaves) {
        if (leaves < TEA_LEAF_COST / 2) {
            return 1;
        }
        if (leaves < TEA_LEAF_COST) {
            return 2;
        }
        if (leaves == TEA_LEAF_COST) {
            return 4;
        }
        return Math.min(5, 3 + (leaves - TEA_LEAF_COST) / 4);
    }
}
