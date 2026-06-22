package dev.ic2port.api.items;

import net.minecraft.world.item.ItemStack;

/**
 * Marks an item that can read EU stats from blocks or electric items.
 */
public interface IEUReader {

    default boolean isEUReader(final ItemStack stack) {
        return true;
    }
}
