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
 * Tier-2 ore crop — yields raw copper.
 */
public class CupricumCrop extends BaseCrop {

    public CupricumCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "cupricum"),
                Component.translatable("crop.ic2port.cupricum"),
                new CropProperties(2, 0, 0, 0, 1, 0),
                new String[]{"copper", "ore", "orange"},
                4);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        int gain = tile.getGainStat();
        int count = 1 + gain / 10;
        return new ItemStack[]{new ItemStack(Items.RAW_COPPER, count)};
    }
}
