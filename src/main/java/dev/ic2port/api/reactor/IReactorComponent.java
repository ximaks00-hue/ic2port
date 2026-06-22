package dev.ic2port.api.reactor;

import net.minecraft.world.item.ItemStack;

/**
 * Item that participates in the reactor simulation tick.
 */
public interface IReactorComponent {

    void processTick(final IReactor reactor, final ItemStack stack, final int x, final int y);
}
