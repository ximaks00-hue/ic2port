package dev.ic2port.crop;

import dev.ic2port.api.crops.ICrop;
import dev.ic2port.item.CropSeedItem;
import dev.ic2port.util.CropSeedHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Vanilla and mod items that can plant a crop on empty crop sticks.
 */
public record BaseSeedEntry(
        ICrop crop,
        int stage,
        int growth,
        int gain,
        int resistance,
        int stackSize) {

    public boolean matches(final ItemStack stack) {
        return !stack.isEmpty() && stack.getCount() >= stackSize;
    }

    public static BaseSeedEntry fromStack(final ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        if (stack.getItem() instanceof CropSeedItem) {
            ICrop crop = CropSeedHelper.getCrop(stack);
            if (crop == null) {
                return null;
            }
            return new BaseSeedEntry(
                    crop,
                    1,
                    CropSeedHelper.getGrowth(stack),
                    CropSeedHelper.getGain(stack),
                    CropSeedHelper.getResistance(stack),
                    1);
        }
        if (stack.is(Items.WHEAT_SEEDS)) {
            return new BaseSeedEntry(CropRegistry.WHEAT, 1, 1, 1, 1, 1);
        }
        if (stack.is(Items.SUGAR_CANE)) {
            return new BaseSeedEntry(CropRegistry.SUGARCANE, 1, 1, 1, 1, 1);
        }
        if (stack.is(Items.NETHER_WART)) {
            return new BaseSeedEntry(CropRegistry.NETHER_WART, 1, 1, 1, 1, 1);
        }
        return null;
    }
}
