package dev.ic2port.crop.builtin;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.BaseCrop;
import dev.ic2port.api.crops.CropProperties;
import dev.ic2port.api.crops.ICropTile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CrimsonFungusCrop extends BaseCrop {

    public CrimsonFungusCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "crimson_fungus"),
                Component.translatable("crop.ic2port.crimson_fungus"),
                new CropProperties(2, 0, 2, 0, 2, 0),
                new String[]{"nether", "fungus", "red"},
                4);
    }

    @Override
    public boolean canProgressGrowth(final ICropTile tile) {
        return tile.getLightLevel() <= 10;
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        return new ItemStack[]{new ItemStack(Items.CRIMSON_FUNGUS, 1 + tile.getGainStat() / 7)};
    }
}
