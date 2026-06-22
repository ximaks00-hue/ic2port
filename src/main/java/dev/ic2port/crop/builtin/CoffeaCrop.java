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
 * Tier-2 food crop — yields cocoa beans (coffee beans substitute).
 */
public class CoffeaCrop extends BaseCrop {

    public CoffeaCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "coffea"),
                Component.translatable("crop.ic2port.coffea"),
                new CropProperties(2, 0, 2, 0, 1, 0),
                new String[]{"coffee", "food", "brown"},
                4);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        int gain = tile.getGainStat();
        int count = 1 + gain / 8;
        return new ItemStack[]{new ItemStack(Items.COCOA_BEANS, count)};
    }
}
