package dev.ic2port.item;

import net.minecraft.world.item.Item;

/**
 * IC2-style circuit component. Three tiers:
 * - Electronic Circuit (LV crafting)
 * - Advanced Circuit (MV crafting)
 * - Complex Circuit (HV crafting)
 */
public class CircuitItem extends Item {

    private final int tier;

    public CircuitItem(final int tier, final Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }
}
