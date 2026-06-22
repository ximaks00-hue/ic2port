package dev.ic2port.api.reactor;

import net.minecraft.world.item.ItemStack;

/**
 * Reactor fuel rod (uranium or MOX) that participates in pulse neighbour counting.
 */
public interface IReactorFuel extends IReactorComponent {

    int getMaxDepletion();

    int getDepletion(ItemStack stack);

    boolean isDepleted(ItemStack stack);
}
