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
 * Tier-2 ore crop — yields redstone dust.
 */
public class CinnabarCrop extends BaseCrop {

    public CinnabarCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "cinnabar"),
                Component.translatable("crop.ic2port.cinnabar"),
                new CropProperties(2, 1, 0, 0, 1, 0),
                new String[]{"redstone", "ore", "red"},
                4);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        int gain = tile.getGainStat();
        int count = 2 + gain / 8;
        return new ItemStack[]{new ItemStack(Items.REDSTONE, count)};
    }
}
