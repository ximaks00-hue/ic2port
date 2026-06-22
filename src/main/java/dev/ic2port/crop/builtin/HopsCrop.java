package dev.ic2port.crop.builtin;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.BaseCrop;
import dev.ic2port.api.crops.CropProperties;
import dev.ic2port.api.crops.ICropTile;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Brewing crop — needs bright light (9+) to grow.
 */
public class HopsCrop extends BaseCrop {

    public HopsCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "hops"),
                Component.translatable("crop.ic2port.hops"),
                new CropProperties(3, 1, 1, 0, 2, 0),
                new String[] {"green", "alcohol", "hop"},
                4);
    }

    @Override
    public int getGrowthDuration(final ICropTile tile) {
        return Math.max(80, 320 - tile.getGrowthStat() * 5);
    }

    @Override
    public boolean canProgressGrowth(final ICropTile tile) {
        return tile.getLightLevel() >= 9;
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        int count = 1 + tile.getGainStat() / 8;
        return new ItemStack[] {new ItemStack(ItemRegistry.HOPS.get(), count)};
    }
}
