package dev.ic2port.util;

import dev.ic2port.item.ElectricItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Shared OD/OV scanner behaviour — IC2 always consumes EU once a scan completes, even with no ores found.
 */
public final class ScannerItemHelper {

    private ScannerItemHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Applies cooldown and draws scan energy after a successful scan attempt (IC2 semantics).
     */
    public static void finalizeScan(final Player player, final Item item, final ItemStack stack,
                                    final ElectricItem electricItem, final double scanCost,
                                    final int cooldownTicks) {
        player.getCooldowns().addCooldown(item, cooldownTicks);
        electricItem.drawEnergy(stack, scanCost);
    }

    public static void showEmptyResult(final Player player) {
        player.displayClientMessage(Component.translatable("message.ic2port.od_scanner.empty"), true);
    }
}
