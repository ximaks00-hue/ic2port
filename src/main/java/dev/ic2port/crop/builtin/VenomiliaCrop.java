package dev.ic2port.crop.builtin;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.BaseCrop;
import dev.ic2port.api.crops.CropProperties;
import dev.ic2port.api.crops.ICropTile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class VenomiliaCrop extends BaseCrop {

    public VenomiliaCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "venomilia"),
                Component.translatable("crop.ic2port.venomilia"),
                new CropProperties(3, 0, 2, 0, 2, 1),
                new String[]{"poison", "purple", "toxic"},
                4);
    }

    @Override
    public boolean canProgressGrowth(final ICropTile tile) {
        return tile.getLightLevel() <= 14;
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        int gain = tile.getGainStat();
        return new ItemStack[]{
                new ItemStack(Items.SPIDER_EYE, 1 + gain / 8),
                new ItemStack(Items.POISONOUS_POTATO, gain >= 10 ? 1 : 0)
        };
    }
}
