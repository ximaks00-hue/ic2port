package dev.ic2port.util;

import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.api.energy.IEnergyNode;
import dev.ic2port.item.IElectricItem;
import dev.ic2port.setup.ModCapabilities;
import net.minecraft.world.item.ItemStack;

/**
 * Helpers for charging and discharging electric items in machine slots.
 */
public final class ItemEnergyHelper {

    /** Charger tier value that skips the minimum-tier gate (not for in-world blocks). */
    public static final int ANY_CHARGER_TIER = 0;

    private ItemEnergyHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * @return whether the stack can accept EU from a charger of unknown tier
     */
    public static boolean canCharge(final ItemStack stack) {
        return canCharge(stack, ANY_CHARGER_TIER);
    }

    public static boolean canCharge(final ItemStack stack, final int chargerTier) {
        if (stack.isEmpty()) {
            return false;
        }
        if (canChargeItemOnly(stack, chargerTier)) {
            return true;
        }
        return ArmorModuleEnergyHelper.canChargeAnyModule(stack, chargerTier);
    }

    private static boolean canChargeItemOnly(final ItemStack stack, final int chargerTier) {
        if (stack.getItem() instanceof IElectricItem electricItem) {
            if (chargerTier != ANY_CHARGER_TIER && chargerTier < electricItem.getTier()) {
                return false;
            }
            return electricItem.getStoredEnergy(stack) < electricItem.getMaxEnergy();
        }
        return stack.getCapability(ModCapabilities.ENERGY_NODE_CAPABILITY)
                .map(node -> {
                    if (!(node instanceof IEnergyAcceptor acceptor)) {
                        return false;
                    }
                    if (chargerTier != ANY_CHARGER_TIER && chargerTier < acceptor.getTier()) {
                        return false;
                    }
                    return acceptor.getStoredEnergy() < acceptor.getCapacity();
                })
                .orElse(false);
    }

    public static boolean canDischarge(final ItemStack stack) {
        if (!(stack.getItem() instanceof IElectricItem electricItem)) {
            return false;
        }
        return electricItem.getStoredEnergy(stack) > 0.0D;
    }

    /**
     * @return whether the item may dump EU into storage of the given tier (includes armor modules)
     */
    public static boolean canDischargeInto(final ItemStack stack, final int storageTier) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof IElectricItem electricItem) {
            if (electricItem.getStoredEnergy(stack) > 0.0D && electricItem.getTier() <= storageTier) {
                return true;
            }
        }
        return ArmorModuleEnergyHelper.canDischargeAnyModule(stack, storageTier);
    }

    /**
     * @return EU transferred into the item
     */
    public static double chargeItem(final ItemStack stack, final double amount, final int tier) {
        if (stack.isEmpty() || amount <= 0.0D) {
            return 0.0D;
        }

        double transferred = 0.0D;
        if (canChargeItemOnly(stack, tier)) {
            IEnergyNode node = stack.getCapability(ModCapabilities.ENERGY_NODE_CAPABILITY).orElse(null);
            if (node instanceof IEnergyAcceptor acceptor) {
                double remainder = acceptor.injectEnergy(null, amount, tier);
                transferred = amount - remainder;
            }
        }
        double remaining = amount - transferred;
        if (remaining > 0.0D) {
            transferred += ArmorModuleEnergyHelper.chargeModules(stack, remaining, tier);
        }
        return transferred;
    }

    /**
     * Extracts EU from an electric item. For storage discharge slots, prefer
     * {@link #dischargeItemAndModules(ItemStack, double, int)} to also drain armor modules.
     *
     * @return EU extracted from the item
     */
    public static double dischargeItem(final ItemStack stack, final double amount) {
        if (stack.isEmpty() || amount <= 0.0D) {
            return 0.0D;
        }
        double drawn = 0.0D;
        if (stack.getItem() instanceof IElectricItem electricItem) {
            drawn = electricItem.drawEnergy(stack, amount);
        }
        return drawn;
    }

    /**
     * Extracts EU from an electric item and, if any budget remains, from any armor modules
     * embedded in the NBT of a nano/quantum chestplate.
     *
     * @return total EU extracted
     */
    public static double dischargeItemAndModules(final ItemStack stack, final double amount, final int storageTier) {
        if (stack.isEmpty() || amount <= 0.0D) {
            return 0.0D;
        }
        double drawn = 0.0D;
        if (stack.getItem() instanceof IElectricItem electricItem
                && electricItem.getTier() <= storageTier) {
            drawn = electricItem.drawEnergy(stack, amount);
        }
        double remaining = amount - drawn;
        if (remaining > 0.0D) {
            drawn += ArmorModuleEnergyHelper.dischargeModules(stack, remaining, storageTier);
        }
        return drawn;
    }
}
