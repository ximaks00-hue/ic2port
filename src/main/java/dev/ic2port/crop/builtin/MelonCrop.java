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
 * Tier-1 food crop — yields melon slices.
 */
public class MelonCrop extends BaseCrop {

    public MelonCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "melon"),
                Component.translatable("crop.ic2port.melon"),
                new CropProperties(1, 0, 3, 0, 2, 0),
                new String[]{"melon", "food", "green"},
                4);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        int gain = tile.getGainStat();
        int count = 2 + gain / 6;
        return new ItemStack[]{new ItemStack(Items.MELON_SLICE, count)};
    }
}
