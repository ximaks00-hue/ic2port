package dev.ic2port.item;

import net.minecraft.world.item.Item;

/**
 * Induction matrix capacitor cell — increases matrix EU storage when installed in the controller GUI.
 */
public class CapacitorCellItem extends Item {

    public static final double CAPACITY_EU = 1_000_000.0D;

    public CapacitorCellItem(final Properties properties) {
        super(properties);
    }
}
