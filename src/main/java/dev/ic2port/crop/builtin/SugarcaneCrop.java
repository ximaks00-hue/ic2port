package dev.ic2port.crop.builtin;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.BaseCrop;
import dev.ic2port.api.crops.CropProperties;
import dev.ic2port.api.crops.ICropTile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Fast-growing crop that yields sugar cane.
 */
public class SugarcaneCrop extends BaseCrop {

    public SugarcaneCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "sugarcane"),
                Component.translatable("crop.ic2port.sugarcane"),
                new CropProperties(1, 0, 2, 0, 1, 1),
                new String[] {"green", "reeds", "sugar"},
                4);
    }

    @Override
    public int getGrowthDuration(final ICropTile tile) {
        return Math.max(30, 120 - tile.getGrowthStat() * 3);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        int count = 1 + tile.getGainStat() / 12;
        return new ItemStack[] {new ItemStack(Items.SUGAR_CANE, count)};
    }
}
