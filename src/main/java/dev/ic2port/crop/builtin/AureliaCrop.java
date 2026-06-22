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
 * Tier-3 ore crop — yields raw gold.
 */
public class AureliaCrop extends BaseCrop {

    public AureliaCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "aurelia"),
                Component.translatable("crop.ic2port.aurelia"),
                new CropProperties(3, 0, 0, 0, 2, 0),
                new String[]{"golden", "ore", "gold"},
                4);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        int gain = tile.getGainStat();
        int count = 1 + gain / 12;
        return new ItemStack[]{new ItemStack(Items.RAW_GOLD, count)};
    }
}
