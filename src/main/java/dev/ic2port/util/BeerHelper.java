package dev.ic2port.util;

import dev.ic2port.brewing.BrewQuality;
import dev.ic2port.item.BeerItem;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * NBT helpers and quality math for brewed beer.
 */
public final class BeerHelper {

    public static final String TAG_QUALITY = "BeerQuality";
    public static final String TAG_ALCOHOL = "BeerAlcohol";

    public static final int HOPS_COST = 4;
    public static final int WHEAT_COST = 4;

    private BeerHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ItemStack createBeer(final int hopsUsed, final int wheatUsed, final int brewTicksRemaining) {
        BrewQuality quality = calculateQuality(hopsUsed, wheatUsed, brewTicksRemaining);
        int alcohol = calculateAlcohol(hopsUsed, wheatUsed, quality);
        ItemStack stack = new ItemStack(ItemRegistry.BEER.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_QUALITY, quality.ordinal());
        tag.putInt(TAG_ALCOHOL, alcohol);
        stack.setHoverName(Component.translatable("item.ic2port.beer." + quality.name().toLowerCase()));
        return stack;
    }

    public static BrewQuality getQuality(final ItemStack stack) {
        if (!(stack.getItem() instanceof BeerItem)) {
            return BrewQuality.BEER;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_QUALITY)) {
            return BrewQuality.BEER;
        }
        return BrewQuality.fromIndex(tag.getInt(TAG_QUALITY));
    }

    public static int getAlcohol(final ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_ALCOHOL)) {
            return 3;
        }
        return tag.getInt(TAG_ALCOHOL);
    }

    public static boolean isBeer(final ItemStack stack) {
        return stack.getItem() instanceof BeerItem && stack.getTag() != null && stack.getTag().contains(TAG_QUALITY);
    }

    static BrewQuality calculateQuality(final int hops, final int wheat, final int brewTicksRemaining) {
        if (hops <= 0 || wheat <= 0) {
            return BrewQuality.BAD;
        }
        if (brewTicksRemaining > 0) {
            return BrewQuality.YOUNGSTER;
        }
        float ratio = (float) hops / (hops + wheat);
        if (ratio < 0.25F) {
            return BrewQuality.BREW;
        }
        if (ratio > 0.65F) {
            return BrewQuality.DRAGONBLOOD;
        }
        if (ratio > 0.5F) {
            return BrewQuality.ALE;
        }
        if (ratio >= 0.35F && ratio <= 0.5F) {
            return BrewQuality.BEER;
        }
        return BrewQuality.BREW;
    }

    private static int calculateAlcohol(final int hops, final int wheat, final BrewQuality quality) {
        if (quality == BrewQuality.BAD || quality == BrewQuality.BREW) {
            return 1;
        }
        if (quality == BrewQuality.YOUNGSTER) {
            return 2;
        }
        int base = 3 + hops / 8;
        if (quality == BrewQuality.DRAGONBLOOD) {
            base = Math.min(6, base + 2);
        }
        if (quality == BrewQuality.ALE) {
            base = Math.min(5, base + 1);
        }
        if (wheat > hops * 2) {
            base = Math.max(1, base - 1);
        }
        return Math.min(6, Math.max(0, base));
    }
}
