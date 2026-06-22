package dev.ic2port.util;

import dev.ic2port.item.IElectricItem;
import dev.ic2port.item.IModulePortableEnergyPack;
import dev.ic2port.item.IPortableEnergyPack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Trickle-charges held electric tools from portable energy packs and armor modules.
 */
public final class PortableEnergyPackHelper {

    private PortableEnergyPackHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void tickInstalledModules(final Player player) {
        ItemStack chestplate = ArmorModuleHelper.getChestplate(player);
        if (!ArmorModuleHelper.acceptsModules(chestplate)) {
            return;
        }

        List<ItemStack> modules = ArmorModuleHelper.getModules(chestplate);
        boolean changed = false;
        for (int index = 0; index < modules.size(); index++) {
            ItemStack module = modules.get(index);
            if (!(module.getItem() instanceof IModulePortableEnergyPack packModule)) {
                continue;
            }
            if (packModule.getModuleStoredEnergy(module) <= 0.0D) {
                continue;
            }
            if (chargeHeldItems(player, module, packModule)) {
                modules.set(index, module);
                changed = true;
            }
        }

        if (changed) {
            ArmorModuleHelper.setModules(chestplate, modules);
            player.setItemSlot(EquipmentSlot.CHEST, chestplate);
        }
    }

    public static void tickChestPack(final Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof IElectricItem electricPack
                && chest.getItem() instanceof IPortableEnergyPack packItem)) {
            return;
        }
        if (electricPack.getStoredEnergy(chest) <= 0.0D) {
            return;
        }

        double remaining = packItem.getChargePerTick();
        for (InteractionHand hand : InteractionHand.values()) {
            if (remaining <= 0.0D) {
                break;
            }
            ItemStack held = player.getItemInHand(hand);
            if (!ItemEnergyHelper.canCharge(held, electricPack.getTier())) {
                continue;
            }
            double transferred = ItemEnergyHelper.chargeItem(held, remaining, electricPack.getTier());
            if (transferred <= 0.0D) {
                continue;
            }
            electricPack.drawEnergy(chest, transferred);
            remaining -= transferred;
        }
    }

    private static boolean chargeHeldItems(
            final Player player,
            final ItemStack moduleStack,
            final IModulePortableEnergyPack packModule) {
        double remaining = packModule.getChargePerTick();
        boolean changed = false;
        for (InteractionHand hand : InteractionHand.values()) {
            if (remaining <= 0.0D) {
                break;
            }
            ItemStack held = player.getItemInHand(hand);
            if (!ItemEnergyHelper.canCharge(held, packModule.getModuleTier())) {
                continue;
            }
            double transferred = ItemEnergyHelper.chargeItem(held, remaining, packModule.getModuleTier());
            if (transferred <= 0.0D) {
                continue;
            }
            packModule.drawModuleEnergy(moduleStack, transferred);
            remaining -= transferred;
            changed = true;
        }
        return changed;
    }
}
