package dev.ic2port.api.crops;

import net.minecraft.world.item.ItemStack;

/**
 * Block entity view of a planted crop.
 */
public interface ICropTile {

    ICrop getCrop();

    void setCrop(ICrop crop);

    int getGrowthStage();

    void setGrowthStage(int stage);

    int getGrowthPoints();

    void setGrowthPoints(int points);

    int getScanLevel();

    void setScanLevel(int level);

    int getGainStat();

    int getGrowthStat();

    int getResistanceStat();

    void setGainStat(int value);

    void setGrowthStat(int value);

    void setResistanceStat(int value);

    int getLightLevel();

    int getHumidity();

    int getNutrients();

    /** Stored hydration bonus (0–150), consumed over time. */
    default int getHydrationStorage() {
        return 0;
    }

    default void setHydrationStorage(final int value) {
    }

    /** Weed-ex protection (0–150); blocks weed conversion while active. */
    default int getWeedExStorage() {
        return 0;
    }

    default void setWeedExStorage(final int value) {
    }

    ItemStack createSeeds(ICrop crop, int growthStat, int gainStat, int resistanceStat, int scanLevel);
}
