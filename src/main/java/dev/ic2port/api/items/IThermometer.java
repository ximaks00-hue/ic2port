package dev.ic2port.api.items;

import net.minecraft.world.item.ItemStack;

/**
 * Marks an item that can read heat from blocks or reactor components.
 */
public interface IThermometer {

    default boolean isThermometer(final ItemStack stack) {
        return true;
    }
}
