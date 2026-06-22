package dev.ic2port.item;

import net.minecraft.world.item.ItemStack;

/**
 * Armor module that stores EU and trickle-charges held tools.
 */
public interface IModulePortableEnergyPack extends IPortableEnergyPack {

    double getModuleCapacity();

    int getModuleTier();

    double getModuleStoredEnergy(ItemStack stack);

    double drawModuleEnergy(ItemStack stack, double amount);
}
