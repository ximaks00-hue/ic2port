package dev.ic2port.crop.builtin;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.BaseCrop;
import dev.ic2port.api.crops.CropProperties;
import dev.ic2port.api.crops.ICropTile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class LumiliaCrop extends BaseCrop {

    public LumiliaCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "lumilia"),
                Component.translatable("crop.ic2port.lumilia"),
                new CropProperties(3, 0, 1, 0, 3, 0),
                new String[] {"light", "flower", "lumilia"},
                4);
    }

    @Override
    public boolean canProgressGrowth(final ICropTile tile) {
        return tile.getLightLevel() >= 12;
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        return new ItemStack[]{new ItemStack(Items.GLOW_BERRIES, 1 + tile.getGainStat() / 8)};
    }
}
