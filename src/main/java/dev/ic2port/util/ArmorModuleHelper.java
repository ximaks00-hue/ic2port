package dev.ic2port.util;

import dev.ic2port.item.NanoSuitItem;
import dev.ic2port.item.QuantumSuitItem;
import dev.ic2port.item.ArmorModuleItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Installed armor modules stored in chestplate NBT (IC2-style module slots).
 */
public final class ArmorModuleHelper {

    public static final String MODULES_TAG = "ArmorModules";

    /** Opens module GUI for the chestplate worn by the player. */
    public static final int WORN_CHEST_INVENTORY_SLOT = -1;

    private ArmorModuleHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean acceptsModules(final ItemStack chestplate) {
        return getMaxModuleSlots(chestplate) > 0;
    }

    public static int getMaxModuleSlots(final ItemStack chestplate) {
        if (!(chestplate.getItem() instanceof ArmorItem armor) || armor.getType() != ArmorItem.Type.CHESTPLATE) {
            return 0;
        }
        if (chestplate.getItem() instanceof QuantumSuitItem) {
            return 4;
        }
        if (chestplate.getItem() instanceof NanoSuitItem) {
            return 2;
        }
        return 0;
    }

    public static List<ItemStack> getModules(final ItemStack chestplate) {
        List<ItemStack> modules = new ArrayList<>();
        if (!acceptsModules(chestplate)) {
            return modules;
        }
        CompoundTag tag = chestplate.getTag();
        if (tag == null || !tag.contains(MODULES_TAG, Tag.TAG_LIST)) {
            return modules;
        }
        ListTag list = tag.getList(MODULES_TAG, Tag.TAG_COMPOUND);
        int max = getMaxModuleSlots(chestplate);
        for (int index = 0; index < Math.min(list.size(), max); index++) {
            modules.add(ItemStack.of(list.getCompound(index)));
        }
        while (modules.size() < max) {
            modules.add(ItemStack.EMPTY);
        }
        return modules;
    }

    public static void setModules(final ItemStack chestplate, final List<ItemStack> modules) {
        if (!acceptsModules(chestplate)) {
            return;
        }
        int max = getMaxModuleSlots(chestplate);
        ListTag list = new ListTag();
        for (int index = 0; index < max; index++) {
            ItemStack module = index < modules.size() ? modules.get(index) : ItemStack.EMPTY;
            if (!module.isEmpty()) {
                list.add(module.save(new CompoundTag()));
            } else {
                list.add(new CompoundTag());
            }
        }
        chestplate.getOrCreateTag().put(MODULES_TAG, list);
    }

    public static boolean tryInstall(final ItemStack chestplate, final ItemStack moduleStack) {
        if (!acceptsModules(chestplate) || !(moduleStack.getItem() instanceof ArmorModuleItem)) {
            return false;
        }
        List<ItemStack> modules = getModules(chestplate);
        for (int index = 0; index < modules.size(); index++) {
            if (modules.get(index).isEmpty()) {
                modules.set(index, moduleStack.copyWithCount(1));
                setModules(chestplate, modules);
                return true;
            }
        }
        return false;
    }

    public static Optional<Integer> findModuleIndex(final ItemStack chestplate, final Class<?> moduleType) {
        if (!acceptsModules(chestplate)) {
            return Optional.empty();
        }
        List<ItemStack> modules = getModules(chestplate);
        for (int index = 0; index < modules.size(); index++) {
            ItemStack module = modules.get(index);
            if (!module.isEmpty() && moduleType.isInstance(module.getItem())) {
                return Optional.of(index);
            }
        }
        return Optional.empty();
    }

    public static void updateModuleAt(
            final Player player,
            final int moduleIndex,
            final ItemStack moduleStack) {
        ItemStack chestplate = getChestplate(player);
        List<ItemStack> modules = getModules(chestplate);
        if (moduleIndex < 0 || moduleIndex >= modules.size()) {
            return;
        }
        modules.set(moduleIndex, moduleStack);
        setModules(chestplate, modules);
        player.setItemSlot(EquipmentSlot.CHEST, chestplate);
    }

    public static Optional<ItemStack> findModule(final ItemStack chestplate, final Class<?> moduleType) {
        for (ItemStack module : getModules(chestplate)) {
            if (!module.isEmpty() && moduleType.isInstance(module.getItem())) {
                return Optional.of(module);
            }
        }
        return Optional.empty();
    }

    public static ItemStack getChestplate(final Player player) {
        return player.getItemBySlot(EquipmentSlot.CHEST);
    }

    public static ItemStack getManagedChestplate(final Inventory inventory, final int chestSlot) {
        if (chestSlot == WORN_CHEST_INVENTORY_SLOT) {
            return inventory.player.getItemBySlot(EquipmentSlot.CHEST);
        }
        return inventory.getItem(chestSlot);
    }

    public static void setManagedChestplate(final Inventory inventory, final int chestSlot, final ItemStack stack) {
        if (chestSlot == WORN_CHEST_INVENTORY_SLOT) {
            inventory.player.setItemSlot(EquipmentSlot.CHEST, stack);
        } else {
            inventory.setItem(chestSlot, stack);
        }
    }
}
