package dev.ic2port.api.reactor;

import net.minecraft.world.item.ItemStack;

/**
 * Reactor component that stores heat in item NBT.
 */
public interface IReactorHeatStorage extends IReactorComponent {

    double getMaxComponentHeat(final ItemStack stack);
}
