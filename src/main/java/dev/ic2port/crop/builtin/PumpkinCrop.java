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
 * Tier-1 food crop — yields pumpkin seeds (drops a pumpkin seed bag).
 */
public class PumpkinCrop extends BaseCrop {

    public PumpkinCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "pumpkin"),
                Component.translatable("crop.ic2port.pumpkin"),
                new CropProperties(1, 0, 2, 0, 2, 0),
                new String[]{"pumpkin", "food", "orange"},
                4);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        int gain = tile.getGainStat();
        int count = 1 + gain / 8;
        return new ItemStack[]{new ItemStack(Items.PUMPKIN_SEEDS, count)};
    }
}
