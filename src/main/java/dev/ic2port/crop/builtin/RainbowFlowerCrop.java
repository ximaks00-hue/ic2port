package dev.ic2port.crop.builtin;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.BaseCrop;
import dev.ic2port.api.crops.CropProperties;
import dev.ic2port.api.crops.ICropTile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class RainbowFlowerCrop extends BaseCrop {

    public RainbowFlowerCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "rainbow_flower"),
                Component.translatable("crop.ic2port.rainbow_flower"),
                new CropProperties(3, 1, 1, 0, 3, 0),
                new String[]{"rainbow", "flower", "color"},
                4);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        int gain = tile.getGainStat();
        ItemStack primary = new ItemStack(Items.POPPY, 1);
        if (gain >= 4) {
            primary = new ItemStack(Items.ALLIUM, 1 + gain / 10);
        }
        ItemStack dye = ItemStack.EMPTY;
        if (gain >= 8) {
            dye = new ItemStack(DyeItem.byColor(net.minecraft.world.item.DyeColor.values()[gain % 16]), 1);
        }
        return dye.isEmpty() ? new ItemStack[]{primary} : new ItemStack[]{primary, dye};
    }
}
