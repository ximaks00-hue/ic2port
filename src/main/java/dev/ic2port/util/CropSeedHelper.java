package dev.ic2port.util;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.ICrop;
import dev.ic2port.crop.CropRegistry;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * NBT helpers for {@link dev.ic2port.item.CropSeedItem}.
 */
public final class CropSeedHelper {

    private static final String CROP_TAG = "CropId";
    private static final String GROWTH_TAG = "Growth";
    private static final String GAIN_TAG = "Gain";
    private static final String RESISTANCE_TAG = "Resistance";
    private static final String SCAN_TAG = "Scan";

    private CropSeedHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ItemStack createSeed(
            final ICrop crop,
            final int growthStat,
            final int gainStat,
            final int resistanceStat,
            final int scanLevel) {
        ItemStack stack = new ItemStack(ItemRegistry.CROP_SEED.get());
        writeCrop(stack, crop.id(), growthStat, gainStat, resistanceStat, scanLevel);
        return stack;
    }

    public static ICrop getCrop(final ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(CROP_TAG)) {
            return null;
        }
        return CropRegistry.get(new ResourceLocation(tag.getString(CROP_TAG)));
    }

    public static int getGrowth(final ItemStack stack) {
        return readStat(stack, GROWTH_TAG, 1);
    }

    public static int getGain(final ItemStack stack) {
        return readStat(stack, GAIN_TAG, 1);
    }

    public static int getResistance(final ItemStack stack) {
        return readStat(stack, RESISTANCE_TAG, 1);
    }

    public static int getScanLevel(final ItemStack stack) {
        return readStat(stack, SCAN_TAG, 0);
    }

    public static void writeCrop(
            final ItemStack stack,
            final ResourceLocation cropId,
            final int growthStat,
            final int gainStat,
            final int resistanceStat,
            final int scanLevel) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(CROP_TAG, cropId.toString());
        tag.putByte(GROWTH_TAG, (byte) clampStat(growthStat));
        tag.putByte(GAIN_TAG, (byte) clampStat(gainStat));
        tag.putByte(RESISTANCE_TAG, (byte) clampStat(resistanceStat));
        tag.putByte(SCAN_TAG, (byte) Math.max(0, Math.min(4, scanLevel)));
    }

    private static int readStat(final ItemStack stack, final String key, final int defaultValue) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(key)) {
            return defaultValue;
        }
        return tag.getByte(key) & 0xFF;
    }

    private static int clampStat(final int value) {
        return Math.max(1, Math.min(31, value));
    }
}
