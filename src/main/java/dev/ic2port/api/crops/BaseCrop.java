package dev.ic2port.api.crops;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Default {@link ICrop} implementation with IC2-like harvest behaviour.
 */
public abstract class BaseCrop implements ICrop {

    private final ResourceLocation id;
    private final Component name;
    private final CropProperties properties;
    private final String[] attributes;
    private final int growthSteps;

    protected BaseCrop(
            final ResourceLocation id,
            final Component name,
            final CropProperties properties,
            final String[] attributes,
            final int growthSteps) {
        this.id = id;
        this.name = name;
        this.properties = properties;
        this.attributes = attributes;
        this.growthSteps = growthSteps;
    }

    @Override
    public ResourceLocation id() {
        return id;
    }

    @Override
    public Component getName() {
        return name;
    }

    @Override
    public CropProperties getProperties() {
        return properties;
    }

    @Override
    public String[] getAttributes() {
        return attributes;
    }

    @Override
    public int getGrowthSteps() {
        return growthSteps;
    }

    @Override
    public int getGrowthDuration(final ICropTile tile) {
        return Math.max(40, getProperties().tier() * 200 - tile.getGrowthStat() * 4);
    }

    @Override
    public boolean canGrow(final ICropTile tile) {
        return tile.getGrowthStage() < getGrowthSteps();
    }

    @Override
    public boolean canBeHarvested(final ICropTile tile) {
        return tile.getGrowthStage() >= getGrowthSteps();
    }

    @Override
    public int getAfterHarvestStage(final ICropTile tile) {
        return 1;
    }

    @Override
    public float getSeedDropChance(final ICropTile tile) {
        if (tile.getGrowthStage() <= 1) {
            return 0.0F;
        }
        float chance = 0.5F;
        if (tile.getGrowthStage() == 2) {
            chance *= 0.5F;
        }
        for (int i = 0; i < getProperties().tier(); i++) {
            chance *= 0.8F;
        }
        if (tile.getGainStat() >= 24) {
            chance *= (float) Math.pow(0.95D, tile.getGainStat() - 23);
        }
        return chance;
    }

    @Override
    public ItemStack getSeeds(final ICropTile tile) {
        return tile.createSeeds(this, tile.getGrowthStat(), tile.getGainStat(), tile.getResistanceStat(), tile.getScanLevel());
    }
}
