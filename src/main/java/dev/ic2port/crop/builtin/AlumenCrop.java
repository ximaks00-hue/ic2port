package dev.ic2port.crop.builtin;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.BaseCrop;
import dev.ic2port.api.crops.CropProperties;
import dev.ic2port.api.crops.ICropTile;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class AlumenCrop extends BaseCrop {

    public AlumenCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "alumen"),
                Component.translatable("crop.ic2port.alumen"),
                new CropProperties(2, 0, 0, 0, 0, 2),
                new String[]{"aluminium", "ore", "alumen"},
                4);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        return new ItemStack[]{new ItemStack(ItemRegistry.TIN_DUST.get(), 1 + tile.getGainStat() / 10)};
    }
}
