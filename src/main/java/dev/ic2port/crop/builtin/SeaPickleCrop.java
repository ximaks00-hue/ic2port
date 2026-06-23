package dev.ic2port.crop.builtin;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.BaseCrop;
import dev.ic2port.api.crops.CropProperties;
import dev.ic2port.api.crops.ICropTile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SeaPickleCrop extends BaseCrop {

    public SeaPickleCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "sea_pickle"),
                Component.translatable("crop.ic2port.sea_pickle"),
                new CropProperties(2, 0, 1, 1, 1, 0),
                new String[]{"green", "sea", "pickle"},
                4);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        return new ItemStack[]{new ItemStack(Items.SEA_PICKLE, 1 + tile.getGainStat() / 8)};
    }
}
