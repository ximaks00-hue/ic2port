package dev.ic2port.crop.builtin;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.BaseCrop;
import dev.ic2port.api.crops.CropProperties;
import dev.ic2port.api.crops.ICropTile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BoneFlowerCrop extends BaseCrop {

    public BoneFlowerCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "bone_flower"),
                Component.translatable("crop.ic2port.bone_flower"),
                new CropProperties(2, 0, 1, 0, 2, 0),
                new String[]{"bone", "white", "flower"},
                4);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        return new ItemStack[]{new ItemStack(Items.BONE_MEAL, 1 + tile.getGainStat() / 6)};
    }
}
