package dev.ic2port.crop.builtin;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.BaseCrop;
import dev.ic2port.api.crops.CropProperties;
import dev.ic2port.api.crops.ICropTile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Unwanted weed that may appear on empty crop sticks.
 */
public class WeedCrop extends BaseCrop {

    public WeedCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "weed"),
                Component.translatable("crop.ic2port.weed"),
                new CropProperties(0, 0, 0, 0, 1, 10),
                new String[] {"weed"},
                3);
    }

    @Override
    public int getGrowthDuration(final ICropTile tile) {
        return 60;
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        return new ItemStack[0];
    }

    @Override
    public float getSeedDropChance(final ICropTile tile) {
        return 0.0F;
    }
}
