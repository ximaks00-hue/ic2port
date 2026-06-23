package dev.ic2port.util;

import dev.ic2port.item.NanoSuitItem;
import dev.ic2port.item.QuantumSuitItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Detects whether a living entity is wearing IC2 electric armor (nano or quantum).
 */
public final class ElectricArmorHelper {

    private ElectricArmorHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean wearsElectricArmor(final LivingEntity entity) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) {
                continue;
            }
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.getItem() instanceof NanoSuitItem || stack.getItem() instanceof QuantumSuitItem) {
                return true;
            }
        }
        return false;
    }
}
