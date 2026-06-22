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
 * Tier-2 ore crop — yields raw tin.
 */
public class StannumCrop extends BaseCrop {

    public StannumCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "stannum"),
                Component.translatable("crop.ic2port.stannum"),
                new CropProperties(2, 0, 0, 0, 0, 0),
                new String[]{"tin", "ore", "grey"},
                4);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        int gain = tile.getGainStat();
        int count = 1 + gain / 10;
        return new ItemStack[]{new ItemStack(ItemRegistry.RAW_TIN.get(), count)};
    }
}
