package dev.ic2port.util;

import dev.ic2port.brewing.PotionQuality;
import dev.ic2port.item.BrewedPotionItem;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * NBT helpers for barrel-brewed potions.
 */
public final class PotionHelper {

    public static final String TAG_QUALITY = "PotionQuality";

    public static final int REDSTONE_COST = 20;
    public static final int GLOWSTONE_COST = 20;
    public static final int BREW_DURATION = 8000;

    private PotionHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ItemStack createPotion(final int redstoneUsed, final int glowstoneUsed) {
        PotionQuality quality = calculateQuality(redstoneUsed, glowstoneUsed);
        ItemStack stack = new ItemStack(ItemRegistry.BREWED_POTION.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_QUALITY, quality.ordinal());
        stack.setHoverName(Component.translatable("item.ic2port.brewed_potion." + quality.name().toLowerCase()));
        return stack;
    }

    public static PotionQuality getQuality(final ItemStack stack) {
        if (!(stack.getItem() instanceof BrewedPotionItem)) {
            return PotionQuality.IMPURE;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_QUALITY)) {
            return PotionQuality.IMPURE;
        }
        return PotionQuality.fromIndex(tag.getInt(TAG_QUALITY));
    }

    private static PotionQuality calculateQuality(final int redstone, final int glowstone) {
        if (redstone <= 0 || glowstone <= 0) {
            return PotionQuality.BAD;
        }
        if (redstone < REDSTONE_COST / 2 || glowstone < GLOWSTONE_COST / 2) {
            return PotionQuality.RAW;
        }
        float ratio = (float) redstone / (redstone + glowstone);
        if (redstone >= REDSTONE_COST && glowstone >= GLOWSTONE_COST && ratio >= 0.45F && ratio <= 0.55F) {
            return PotionQuality.PURE;
        }
        if (redstone >= REDSTONE_COST && glowstone >= GLOWSTONE_COST) {
            if (ratio > 0.65F) {
                return PotionQuality.REDUCED;
            }
            if (ratio < 0.35F) {
                return PotionQuality.CONCENTRATED;
            }
            return PotionQuality.IMPURE;
        }
        if (ratio > 0.6F) {
            return PotionQuality.UNREFINED;
        }
        return PotionQuality.IMPURE;
    }
}
