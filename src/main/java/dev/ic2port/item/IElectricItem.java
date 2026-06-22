package dev.ic2port.item;

import net.minecraft.world.item.ItemStack;

/**
 * Contract for IC2 items that store EU in their {@link ItemStack} NBT.
 */
public interface IElectricItem {

    double getMaxEnergy();

    int getTier();

    double getStoredEnergy(ItemStack stack);

    void setStoredEnergy(ItemStack stack, double energy);

    /**
     * @return remainder that could not be accepted
     */
    double charge(ItemStack stack, double amount);

    /**
     * @return EU actually extracted from the stack
     */
    double drawEnergy(ItemStack stack, double amount);
}
