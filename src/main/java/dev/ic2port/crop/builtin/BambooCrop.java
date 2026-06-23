package dev.ic2port.crop.builtin;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.BaseCrop;
import dev.ic2port.api.crops.CropProperties;
import dev.ic2port.api.crops.ICropTile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BambooCrop extends BaseCrop {

    public BambooCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "bamboo"),
                Component.translatable("crop.ic2port.bamboo"),
                new CropProperties(3, 1, 1, 0, 2, 0),
                new String[]{"green", "reed", "bamboo"},
                4);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        return new ItemStack[]{new ItemStack(Items.BAMBOO, 1 + tile.getGainStat() / 6)};
    }
}
