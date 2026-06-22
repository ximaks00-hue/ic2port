package dev.ic2port.util;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

/**
 * Validates that each armor slot holds the correct piece type for a suit set.
 */
public final class ArmorSetHelper {

    private ArmorSetHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean hasFullTypedSet(final Player player, final Class<? extends ArmorItem> armorClass) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) {
                continue;
            }
            ItemStack stack = player.getItemBySlot(slot);
            if (!(stack.getItem() instanceof ArmorItem armor) || !armorClass.isInstance(armor)) {
                return false;
            }
            if (armor.getType() != typeForSlot(slot)) {
                return false;
            }
        }
        return true;
    }

    private static ArmorItem.Type typeForSlot(final EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> ArmorItem.Type.HELMET;
            case CHEST -> ArmorItem.Type.CHESTPLATE;
            case LEGS -> ArmorItem.Type.LEGGINGS;
            case FEET -> ArmorItem.Type.BOOTS;
            default -> throw new IllegalArgumentException("Not an armor slot: " + slot);
        };
    }
}
