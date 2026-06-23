package dev.ic2port.crop.builtin;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.BaseCrop;
import dev.ic2port.api.crops.CropProperties;
import dev.ic2port.api.crops.ICropTile;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class TeaCrop extends BaseCrop {

    public TeaCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "tea"),
                Component.translatable("crop.ic2port.tea"),
                new CropProperties(2, 1, 0, 0, 2, 0),
                new String[]{"tea", "green", "drink"},
                4);
    }

    @Override
    public boolean canProgressGrowth(final ICropTile tile) {
        return tile.getLightLevel() >= 8;
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        return new ItemStack[]{new ItemStack(ItemRegistry.TEA_LEAF.get(), 1 + tile.getGainStat() / 8)};
    }
}
