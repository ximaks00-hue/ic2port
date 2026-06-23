package dev.ic2port.crop.builtin;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.BaseCrop;
import dev.ic2port.api.crops.CropProperties;
import dev.ic2port.api.crops.ICropTile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GlowshroomCrop extends BaseCrop {

    public GlowshroomCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "glowshroom"),
                Component.translatable("crop.ic2port.glowshroom"),
                new CropProperties(2, 1, 0, 1, 2, 0),
                new String[] {"glow", "mushroom", "light"},
                4);
    }

    @Override
    public boolean canProgressGrowth(final ICropTile tile) {
        return tile.getLightLevel() <= 10;
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        return new ItemStack[]{new ItemStack(Items.GLOWSTONE_DUST, 1 + tile.getGainStat() / 7)};
    }
}
