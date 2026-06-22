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
 * Tier-2 defensive crop — yields cactus blocks.
 */
public class CactusCrop extends BaseCrop {

    public CactusCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "cactus"),
                Component.translatable("crop.ic2port.cactus"),
                new CropProperties(2, 1, 0, 2, 1, 0),
                new String[]{"cactus", "defensive", "green"},
                4);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        int gain = tile.getGainStat();
        int count = 1 + gain / 8;
        return new ItemStack[]{new ItemStack(Items.CACTUS, count)};
    }
}
