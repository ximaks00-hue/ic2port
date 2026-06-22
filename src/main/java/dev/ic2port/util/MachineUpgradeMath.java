package dev.ic2port.util;

import dev.ic2port.item.EnergyStorageUpgradeItem;
import dev.ic2port.item.OverclockerUpgradeItem;
import dev.ic2port.item.TransformerUpgradeItem;

/**
 * IC2 upgrade scaling formulas.
 */
public final class MachineUpgradeMath {

    private static final double SPEED_FACTOR = 0.7D;
    private static final double POWER_FACTOR = 1.6D;

    private MachineUpgradeMath() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static int scaledProcessTime(final int baseProcessTime, final int overclockerCount) {
        if (overclockerCount <= 0) {
            return baseProcessTime;
        }
        return Math.max(1, (int) Math.round(baseProcessTime * Math.pow(SPEED_FACTOR, overclockerCount)));
    }

    public static double scaledEnergyPerTick(final double baseEnergyPerTick, final int overclockerCount) {
        if (overclockerCount <= 0) {
            return baseEnergyPerTick;
        }
        return baseEnergyPerTick * Math.pow(POWER_FACTOR, overclockerCount);
    }

    public static int countOverclockers(final net.minecraftforge.items.IItemHandler handler, final int upgradeSlotStart) {
        return countUpgradeItems(handler, upgradeSlotStart, OverclockerUpgradeItem.class);
    }

    public static int countTransformerUpgrades(final net.minecraftforge.items.IItemHandler handler, final int upgradeSlotStart) {
        return countUpgradeItems(handler, upgradeSlotStart, TransformerUpgradeItem.class);
    }

    public static int countEnergyStorageUpgrades(final net.minecraftforge.items.IItemHandler handler, final int upgradeSlotStart) {
        return countUpgradeItems(handler, upgradeSlotStart, EnergyStorageUpgradeItem.class);
    }

    private static int countUpgradeItems(
            final net.minecraftforge.items.IItemHandler handler,
            final int upgradeSlotStart,
            final Class<? extends net.minecraft.world.item.Item> upgradeType) {
        int count = 0;
        for (int slot = upgradeSlotStart; slot < handler.getSlots(); slot++) {
            net.minecraft.world.item.ItemStack stack = handler.getStackInSlot(slot);
            if (upgradeType.isInstance(stack.getItem())) {
                count += stack.getCount();
            }
        }
        return count;
    }
}
