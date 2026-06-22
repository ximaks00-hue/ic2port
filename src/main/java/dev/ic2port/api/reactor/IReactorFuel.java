package dev.ic2port.api.reactor;

import net.minecraft.world.item.ItemStack;

/**
 * Reactor fuel rod (uranium or MOX) that participates in pulse neighbour counting.
 */
public interface IReactorFuel extends IReactorComponent {

    int getMaxDepletion();

    int getDepletion(ItemStack stack);

    boolean isDepleted(ItemStack stack);

    /** Individual rod count represented by this stack for adjacency/pulse math (dual=2, quad=4). */
    default int getRodCount() {
        return 1;
    }
}
