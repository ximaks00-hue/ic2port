package dev.ic2port.crop.builtin;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.BaseCrop;
import dev.ic2port.api.crops.CropProperties;
import dev.ic2port.api.crops.ICropTile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Generic crop that drops a vanilla item with optional light requirements.
 */
public class VanillaDropCrop extends BaseCrop {

    public enum LightMode {
        ANY,
        LOW_LIGHT,
        BRIGHT
    }

    private final Item dropItem;
    private final int gainDivisor;
    private final LightMode lightMode;

    public VanillaDropCrop(
            final String id,
            final CropProperties properties,
            final String[] attributes,
            final Item dropItem,
            final int gainDivisor,
            final LightMode lightMode) {
        super(
                new ResourceLocation(Reference.MOD_ID, id),
                Component.translatable("crop.ic2port." + id),
                properties,
                attributes,
                4);
        this.dropItem = dropItem;
        this.gainDivisor = Math.max(1, gainDivisor);
        this.lightMode = lightMode;
    }

    @Override
    public boolean canProgressGrowth(final ICropTile tile) {
        return switch (lightMode) {
            case LOW_LIGHT -> tile.getLightLevel() <= 12;
            case BRIGHT -> tile.getLightLevel() >= 9;
            case ANY -> true;
        };
    }

    @Override
    public ItemStack[] getDrops(final ICropTile tile) {
        int count = 1 + tile.getGainStat() / gainDivisor;
        return new ItemStack[]{new ItemStack(dropItem, count)};
    }
}
