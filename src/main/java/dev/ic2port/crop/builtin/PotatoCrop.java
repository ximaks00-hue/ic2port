package dev.ic2port.crop.builtin;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.BaseCrop;
import dev.ic2port.api.crops.CropProperties;
import dev.ic2port.api.crops.ICropTile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PotatoCrop extends BaseCrop {

    public PotatoCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "potato"),
                Component.translatable("crop.ic2port.potato"),
                new CropProperties(2, 1, 1, 0, 1, 0),
                new String[]{"food", "potato", "brown"},
                4);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        return new ItemStack[]{new ItemStack(Items.POTATO, 1 + tile.getGainStat() / 8)};
    }
}
