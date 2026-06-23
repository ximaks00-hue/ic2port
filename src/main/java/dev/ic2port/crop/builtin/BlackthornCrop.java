package dev.ic2port.crop.builtin;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.BaseCrop;
import dev.ic2port.api.crops.CropProperties;
import dev.ic2port.api.crops.ICropTile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BlackthornCrop extends BaseCrop {

    public BlackthornCrop() {
        super(
                new ResourceLocation(Reference.MOD_ID, "blackthorn"),
                Component.translatable("crop.ic2port.blackthorn"),
                new CropProperties(2, 0, 0, 3, 0, 1),
                new String[] {"thorn", "defensive", "blackthorn"},
                4);
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        int sticks = 1 + tile.getResistanceStat() / 8;
        int saplings = tile.getGainStat() >= 16 ? 1 : 0;
        if (saplings > 0) {
            return new ItemStack[] {
                    new ItemStack(Items.STICK, sticks),
                    new ItemStack(Items.OAK_SAPLING, saplings)
            };
        }
        return new ItemStack[]{new ItemStack(Items.STICK, sticks)};
    }
}
