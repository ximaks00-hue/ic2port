package dev.ic2port.util;

import dev.ic2port.api.reactor.IReactorHeatStorage;
import dev.ic2port.api.reactor.IReactorMonitor;
import dev.ic2port.blockentity.BrewingBarrelBlockEntity;
import dev.ic2port.blockentity.ThermalCentrifugeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Resolves heat readings for the {@link dev.ic2port.item.ThermometerItem}.
 */
public final class ThermometerHelper {

    private ThermometerHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean measureBlock(final Level level, final BlockPos pos, final Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof IReactorMonitor monitor) {
            sendHeat(
                    player,
                    (int) Math.round(monitor.getHeat()),
                    (int) Math.round(monitor.getMaxHeat()),
                    monitor.isActive()
                            ? "message.ic2port.thermometer.reactor_active"
                            : "message.ic2port.thermometer.reactor_inactive");
            return true;
        }
        if (blockEntity instanceof ThermalCentrifugeBlockEntity centrifuge) {
            sendHeat(
                    player,
                    (int) Math.round(centrifuge.getRotorHeat()),
                    (int) Math.round(centrifuge.getMaxRotorHeat()),
                    "message.ic2port.thermometer.centrifuge");
            return true;
        }
        if (blockEntity instanceof BrewingBarrelBlockEntity barrel) {
            sendHeat(
                    player,
                    barrel.getTemperature(),
                    40,
                    barrel.isBrewing()
                            ? "message.ic2port.thermometer.barrel_active"
                            : "message.ic2port.thermometer.barrel_idle");
            return true;
        }
        return false;
    }

    public static boolean measureComponent(final ItemStack stack, final Player player) {
        if (!(stack.getItem() instanceof IReactorHeatStorage heatStorage)) {
            return false;
        }
        sendHeat(
                player,
                (int) Math.round(ReactorComponentHeat.getHeat(stack)),
                (int) Math.round(heatStorage.getMaxComponentHeat(stack)),
                "message.ic2port.thermometer.component");
        return true;
    }

    private static void sendHeat(
            final Player player,
            final int heat,
            final int maxHeat,
            final String contextKey) {
        player.displayClientMessage(Component.translatable(contextKey), true);
        player.displayClientMessage(
                Component.translatable("message.ic2port.thermometer.heat", heat, maxHeat),
                true);
    }
}
