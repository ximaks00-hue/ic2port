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
 * Tier-2 food/dye crop — yields cocoa beans.
 */
public class CocoaCrop extends BaseCrop {

    public CocoaCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "cocoa"),
                Component.translatable("crop.ic2port.cocoa"),
                new CropProperties(2, 0, 2, 0, 2, 0),
                new String[]{"cocoa", "food", "brown", "colorful"},
                4);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        int gain = tile.getGainStat();
        int count = 2 + gain / 8;
        return new ItemStack[]{new ItemStack(Items.COCOA_BEANS, count)};
    }
}
