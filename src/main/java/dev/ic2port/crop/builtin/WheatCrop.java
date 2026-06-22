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
 * Tier-1 starter crop — mirrors vanilla wheat on crop sticks.
 */
public class WheatCrop extends BaseCrop {

    public WheatCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "wheat"),
                Component.translatable("crop.ic2port.wheat"),
                new CropProperties(1, 0, 4, 0, 0, 0),
                new String[] {"yellow", "food", "wheat"},
                4);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        int gain = tile.getGainStat();
        int wheatCount = 1 + gain / 8;
        return new ItemStack[] {new ItemStack(Items.WHEAT, wheatCount)};
    }
}
