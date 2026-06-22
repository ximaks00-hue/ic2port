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
 * Tier-2 ore crop — yields iron ore dust (raw iron for now).
 */
public class FerroCrop extends BaseCrop {

    public FerroCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "ferru"),
                Component.translatable("crop.ic2port.ferru"),
                new CropProperties(2, 0, 0, 0, 0, 0),
                new String[]{"ferrous", "ore", "iron"},
                4);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        int gain = tile.getGainStat();
        int count = 1 + gain / 10;
        return new ItemStack[]{new ItemStack(Items.RAW_IRON, count)};
    }
}
