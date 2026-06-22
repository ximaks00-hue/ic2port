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
 * Terra wart — grows faster on snow blocks under farmland; edible antidote.
 */
public class TerraWartCrop extends BaseCrop {

    public TerraWartCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "terra_wart"),
                Component.translatable("crop.ic2port.terra_wart"),
                new CropProperties(2, 0, 0, 2, 0, 3),
                new String[] {"blue", "nether", "wheat"},
                4);
    }

    @Override
    public int getGrowthDuration(final ICropTile tile) {
        return Math.max(50, 200 - tile.getGrowthStat() * 4);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        int count = 1 + tile.getGainStat() / 10;
        return new ItemStack[] {new ItemStack(ItemRegistry.TERRA_WART.get(), count)};
    }
}
