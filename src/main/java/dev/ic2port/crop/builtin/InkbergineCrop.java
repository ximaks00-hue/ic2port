package dev.ic2port.crop.builtin;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.BaseCrop;
import dev.ic2port.api.crops.CropProperties;
import dev.ic2port.api.crops.ICropTile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class InkbergineCrop extends BaseCrop {

    public InkbergineCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "inkbergine"),
                Component.translatable("crop.ic2port.inkbergine"),
                new CropProperties(2, 2, 0, 0, 1, 0),
                new String[] {"ink", "purple", "inkbergine"},
                4);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        return new ItemStack[]{new ItemStack(Items.INK_SAC, 1 + tile.getGainStat() / 6)};
    }
}
