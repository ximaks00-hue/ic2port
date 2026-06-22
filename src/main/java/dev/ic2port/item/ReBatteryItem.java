package dev.ic2port.item;

import dev.ic2port.api.energy.EnergyTier;

/**
 * Portable LV energy storage — 10 000 EU capacity.
 */
public class ReBatteryItem extends ElectricItem {

    public static final double CAPACITY = 10000.0D;

    public ReBatteryItem(final Properties properties) {
        super(properties.stacksTo(1), CAPACITY, EnergyTier.LV);
    }
}
