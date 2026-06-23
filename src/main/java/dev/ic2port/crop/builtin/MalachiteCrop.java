package dev.ic2port.crop.builtin;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.BaseCrop;
import dev.ic2port.api.crops.CropProperties;
import dev.ic2port.api.crops.ICropTile;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class MalachiteCrop extends BaseCrop {

    public MalachiteCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "malachite"),
                Component.translatable("crop.ic2port.malachite"),
                new CropProperties(3, 0, 0, 0, 1, 1),
                new String[]{"copper", "ore", "green"},
                4);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        return new ItemStack[]{new ItemStack(ItemRegistry.COPPER_DUST.get(), 1 + tile.getGainStat() / 10)};
    }
}
