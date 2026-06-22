package dev.ic2port.api.crops;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Contract for a plant grown on {@link dev.ic2port.block.CropSticksBlock}.
 */
public interface ICrop {

    ResourceLocation id();

    Component getName();

    CropProperties getProperties();

    String[] getAttributes();

    int getGrowthSteps();

    int getGrowthDuration(ICropTile tile);

    boolean canGrow(ICropTile tile);

    boolean canBeHarvested(ICropTile tile);

    int getAfterHarvestStage(ICropTile tile);

    ItemStack[] getDrops(ICropTile tile);

    float getSeedDropChance(ICropTile tile);

    ItemStack getSeeds(ICropTile tile);

    /** When false, the crop stalls until conditions improve (e.g. hops need light 9+). */
    default boolean canProgressGrowth(final ICropTile tile) {
        return true;
    }

    default boolean canBreed(ICropTile tile) {
        return tile.getGrowthStage() >= 3;
    }
}
