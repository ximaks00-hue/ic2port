package dev.ic2port.util;

import dev.ic2port.item.ElectricJetpackItem;
import dev.ic2port.item.IElectricItem;
import dev.ic2port.item.JetpackModuleItem;
import dev.ic2port.item.ArmorModuleItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * Jetpack thrust shared by {@link ElectricJetpackItem} and installed {@link JetpackModuleItem}.
 */
public final class JetpackHelper {

    private JetpackHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean applyThrust(
            final Player player,
            final ItemStack energyStack,
            final double capacity,
            final ElectricJetpackItem.JetpackMode mode) {
        if (getStoredEnergy(energyStack, capacity) <= ElectricJetpackItem.MIN_ACTIVE_ENERGY) {
            return false;
        }
        if (player.onGround() || player.isInWater() || player.isPassenger()) {
            return false;
        }

        double cost = mode.getEnergyPerTick();
        if (mode == ElectricJetpackItem.JetpackMode.NORMAL) {
            if (!PlayerInputHelper.isJumping(player)) {
                return false;
            }
            if (!drawEnergyCost(energyStack, capacity, cost)) {
                return false;
            }
            var motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x, 0.45D, motion.z);
            return true;
        }

        if (!drawEnergyCost(energyStack, capacity, cost)) {
            return false;
        }
        var motion = player.getDeltaMovement();
        if (PlayerInputHelper.isJumping(player)) {
            player.setDeltaMovement(motion.x, 0.12D, motion.z);
        } else if (motion.y < -0.15D) {
            player.setDeltaMovement(motion.x, -0.15D, motion.z);
            player.fallDistance = 0.0F;
        }
        return true;
    }

    private static double getStoredEnergy(final ItemStack stack, final double moduleCapacity) {
        if (stack.getItem() instanceof IElectricItem electric) {
            return electric.getStoredEnergy(stack);
        }
        return ModuleEnergyHelper.getStoredEnergy(stack, moduleCapacity);
    }

    private static boolean drawEnergyCost(final ItemStack stack, final double moduleCapacity, final double cost) {
        if (stack.getItem() instanceof IElectricItem electric) {
            return electric.drawEnergy(stack, cost) >= cost;
        }
        return ModuleEnergyHelper.drawEnergy(stack, moduleCapacity, cost) >= cost;
    }

    public static Optional<Integer> findInstalledJetpackIndex(final ItemStack chestplate) {
        if (!ArmorModuleHelper.acceptsModules(chestplate)) {
            return Optional.empty();
        }
        List<ItemStack> modules = ArmorModuleHelper.getModules(chestplate);
        for (int index = 0; index < modules.size(); index++) {
            if (modules.get(index).getItem() instanceof JetpackModuleItem) {
                return Optional.of(index);
            }
        }
        return Optional.empty();
    }

    public static boolean cycleInstalledJetpackMode(final Player player) {
        ItemStack chestplate = ArmorModuleHelper.getChestplate(player);
        Optional<Integer> index = findInstalledJetpackIndex(chestplate);
        if (index.isEmpty()) {
            return false;
        }
        List<ItemStack> modules = ArmorModuleHelper.getModules(chestplate);
        ItemStack module = modules.get(index.get());
        if (!(module.getItem() instanceof JetpackModuleItem jetpackModule)) {
            return false;
        }
        ElectricJetpackItem.JetpackMode next = jetpackModule.getMode(module).next();
        jetpackModule.setMode(module, next);
        modules.set(index.get(), module);
        ArmorModuleHelper.setModules(chestplate, modules);
        player.setItemSlot(EquipmentSlot.CHEST, chestplate);
        player.displayClientMessage(
                Component.empty()
                        .append(Component.translatable("item.ic2port.electric_jetpack.mode_switch_prefix"))
                        .append(Component.translatable(next.getTranslationKey()).withStyle(next.getChatColor())),
                true);
        return true;
    }
}
