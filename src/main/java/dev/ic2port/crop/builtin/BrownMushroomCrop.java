package dev.ic2port.crop.builtin;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.BaseCrop;
import dev.ic2port.api.crops.CropProperties;
import dev.ic2port.api.crops.ICropTile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BrownMushroomCrop extends BaseCrop {

    public BrownMushroomCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "brown_mushroom"),
                Component.translatable("crop.ic2port.brown_mushroom"),
                new CropProperties(2, 0, 1, 0, 1, 0),
                new String[]{"brown", "mushroom", "food"},
                4);
    }

    @Override
    public boolean canProgressGrowth(final ICropTile tile) {
        return tile.getLightLevel() <= 12;
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        return new ItemStack[]{new ItemStack(Items.BROWN_MUSHROOM, 1 + tile.getGainStat() / 8)};
    }
}
