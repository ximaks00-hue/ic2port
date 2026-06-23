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
 * Tier-4 endgame crop — rare iridium dust yield.
 */
public class AdamantumCrop extends BaseCrop {

    public AdamantumCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "adamantum"),
                Component.translatable("crop.ic2port.adamantum"),
                new CropProperties(4, 2, 0, 2, 1, 0),
                new String[] {"adamantum", "metal", "endgame"},
                5);
    }

    @Override
    public int getGrowthDuration(final ICropTile tile) {
        return Math.max(120, 500 - tile.getGrowthStat() * 6);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        return new ItemStack[]{new ItemStack(ItemRegistry.IRIDIUM.get(), 1 + tile.getGainStat() / 20)};
    }
}
