package dev.ic2port.util;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.item.EnergyShieldModuleItem;
import dev.ic2port.item.IModulePortableEnergyPack;
import dev.ic2port.item.JetpackModuleItem;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Charges EU stored inside armor module items nested in nano / quantum chestplates.
 */
public final class ArmorModuleEnergyHelper {

    private ArmorModuleEnergyHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean canChargeAnyModule(final ItemStack chestplate, final int chargerTier) {
        if (!ArmorModuleHelper.acceptsModules(chestplate)) {
            return false;
        }
        for (ItemStack module : ArmorModuleHelper.getModules(chestplate)) {
            if (canChargeModule(module, chargerTier)) {
                return true;
            }
        }
        return false;
    }

    public static double chargeModules(final ItemStack chestplate, final double amount, final int chargerTier) {
        if (amount <= 0.0D || !ArmorModuleHelper.acceptsModules(chestplate)) {
            return 0.0D;
        }

        List<ItemStack> modules = ArmorModuleHelper.getModules(chestplate);
        double used = 0.0D;
        for (int index = 0; index < modules.size(); index++) {
            ItemStack module = modules.get(index);
            if (module.isEmpty()) {
                continue;
            }
            double remaining = amount - used;
            if (remaining <= 0.0D) {
                break;
            }
            double accepted = chargeModule(module, remaining, chargerTier);
            if (accepted > 0.0D) {
                modules.set(index, module);
                used += accepted;
            }
        }

        if (used > 0.0D) {
            ArmorModuleHelper.setModules(chestplate, modules);
        }
        return used;
    }

    public static boolean canDischargeAnyModule(final ItemStack chestplate, final int storageTier) {
        if (!ArmorModuleHelper.acceptsModules(chestplate)) {
            return false;
        }
        for (ItemStack module : ArmorModuleHelper.getModules(chestplate)) {
            if (canDischargeModule(module, storageTier)) {
                return true;
            }
        }
        return false;
    }

    public static double dischargeModules(final ItemStack chestplate, final double amount, final int storageTier) {
        if (amount <= 0.0D || !ArmorModuleHelper.acceptsModules(chestplate)) {
            return 0.0D;
        }

        List<ItemStack> modules = ArmorModuleHelper.getModules(chestplate);
        double drawn = 0.0D;
        for (int index = 0; index < modules.size(); index++) {
            ItemStack module = modules.get(index);
            if (module.isEmpty()) {
                continue;
            }
            double remaining = amount - drawn;
            if (remaining <= 0.0D) {
                break;
            }
            double drained = dischargeModule(module, remaining, storageTier);
            if (drained > 0.0D) {
                modules.set(index, module);
                drawn += drained;
            }
        }

        if (drawn > 0.0D) {
            ArmorModuleHelper.setModules(chestplate, modules);
        }
        return drawn;
    }

    private static boolean canDischargeModule(final ItemStack module, final int storageTier) {
        ModuleEnergyStats stats = getStats(module);
        if (stats == null || stats.tier() > storageTier) {
            return false;
        }
        return ModuleEnergyHelper.getStoredEnergy(module, stats.capacity()) > 0.0D;
    }

    private static double dischargeModule(final ItemStack module, final double amount, final int storageTier) {
        ModuleEnergyStats stats = getStats(module);
        if (stats == null || stats.tier() > storageTier) {
            return 0.0D;
        }
        return ModuleEnergyHelper.drawEnergy(module, stats.capacity(), amount);
    }

    private static boolean canChargeModule(final ItemStack module, final int chargerTier) {
        ModuleEnergyStats stats = getStats(module);
        if (stats == null || chargerTier < stats.tier()) {
            return false;
        }
        return ModuleEnergyHelper.getStoredEnergy(module, stats.capacity()) < stats.capacity();
    }

    private static double chargeModule(final ItemStack module, final double amount, final int chargerTier) {
        ModuleEnergyStats stats = getStats(module);
        if (stats == null || chargerTier < stats.tier()) {
            return 0.0D;
        }
        double stored = ModuleEnergyHelper.getStoredEnergy(module, stats.capacity());
        double space = stats.capacity() - stored;
        double accepted = Math.min(amount, space);
        if (accepted <= 0.0D) {
            return 0.0D;
        }
        ModuleEnergyHelper.setStoredEnergy(module, stats.capacity(), stored + accepted);
        return accepted;
    }

    private static ModuleEnergyStats getStats(final ItemStack module) {
        if (module.getItem() instanceof IModulePortableEnergyPack pack) {
            return new ModuleEnergyStats(pack.getModuleCapacity(), pack.getModuleTier());
        }
        if (module.getItem() instanceof EnergyShieldModuleItem) {
            return new ModuleEnergyStats(EnergyShieldModuleItem.CAPACITY, EnergyTier.MV);
        }
        if (module.getItem() instanceof JetpackModuleItem) {
            return new ModuleEnergyStats(JetpackModuleItem.CAPACITY, EnergyTier.LV);
        }
        return null;
    }

    private record ModuleEnergyStats(double capacity, int tier) {
    }
}
