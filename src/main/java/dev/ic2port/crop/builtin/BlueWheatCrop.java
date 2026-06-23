package dev.ic2port.crop.builtin;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.BaseCrop;
import dev.ic2port.api.crops.CropProperties;
import dev.ic2port.api.crops.ICropTile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BlueWheatCrop extends BaseCrop {

    public BlueWheatCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "blue_wheat"),
                Component.translatable("crop.ic2port.blue_wheat"),
                new CropProperties(3, 1, 0, 0, 2, 0),
                new String[]{"blue", "wheat", "food"},
                4);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        int gain = tile.getGainStat();
        return new ItemStack[]{
                new ItemStack(Items.WHEAT, 1 + gain / 6),
                new ItemStack(Items.LAPIS_LAZULI, gain >= 6 ? 1 : 0)
        };
    }
}
