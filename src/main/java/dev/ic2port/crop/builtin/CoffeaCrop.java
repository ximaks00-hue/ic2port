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
 * Tier-2 food crop — yields coffee beans for barrel brewing.
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
        return new ItemStack[]{new ItemStack(ItemRegistry.COFFEE_BEAN.get(), count)};
    }
}
