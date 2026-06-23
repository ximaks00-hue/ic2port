package dev.ic2port.crop.builtin;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.BaseCrop;
import dev.ic2port.api.crops.CropProperties;
import dev.ic2port.api.crops.ICropTile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class KelpCrop extends BaseCrop {

    public KelpCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "kelp"),
                Component.translatable("crop.ic2port.kelp"),
                new CropProperties(1, 0, 2, 1, 1, 0),
                new String[]{"green", "water", "kelp"},
                4);
    }

    @Override
    public boolean canProgressGrowth(final ICropTile tile) {
        return tile.getLightLevel() >= 6;
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        return new ItemStack[]{new ItemStack(Items.KELP, 1 + tile.getGainStat() / 5)};
    }
}
