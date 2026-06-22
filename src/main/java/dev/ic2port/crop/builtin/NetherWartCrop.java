package dev.ic2port.crop.builtin;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.BaseCrop;
import dev.ic2port.api.crops.CropProperties;
import dev.ic2port.api.crops.ICropTile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Nether wart on crop sticks — grows faster on soul sand under the farmland.
 */
public class NetherWartCrop extends BaseCrop {

    public NetherWartCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "nether_wart"),
                Component.translatable("crop.ic2port.nether_wart"),
                new CropProperties(2, 0, 0, 2, 0, 3),
                new String[] {"red", "nether", "wheat"},
                4);
    }

    @Override
    public int getGrowthDuration(final ICropTile tile) {
        return Math.max(50, 200 - tile.getGrowthStat() * 4);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        int count = 1 + tile.getGainStat() / 10;
        return new ItemStack[] {new ItemStack(Items.NETHER_WART, count)};
    }
}
