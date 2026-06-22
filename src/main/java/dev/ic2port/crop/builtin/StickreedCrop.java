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
 * Tier-2 crop that yields sticky resin for rubber production.
 */
public class StickreedCrop extends BaseCrop {

    public StickreedCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "stickreed"),
                Component.translatable("crop.ic2port.stickreed"),
                new CropProperties(2, 2, 0, 0, 1, 0),
                new String[] {"green", "resin", "reeds"},
                4);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        int resin = 1 + tile.getGainStat() / 10;
        return new ItemStack[] {new ItemStack(ItemRegistry.STICKY_RESIN.get(), resin)};
    }
}
