package dev.ic2port.util;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyEmitter;
import dev.ic2port.api.energy.IEnergyNode;
import dev.ic2port.blockentity.BaseCableBlockEntity;
import dev.ic2port.item.IElectricItem;
import dev.ic2port.setup.ModCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves EU readings for the {@link dev.ic2port.item.EuReaderItem}.
 */
public final class EuReaderHelper {

    private EuReaderHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean measureBlock(final Level level, final BlockPos pos, final Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return false;
        }

        IEnergyNode node = blockEntity.getCapability(ModCapabilities.ENERGY_NODE_CAPABILITY, null).orElse(null);
        if (node == null && blockEntity instanceof IEnergyNode direct) {
            node = direct;
        }
        if (node == null) {
            return false;
        }

        sendNodeStats(player, node);
        appendCableStats(player, blockEntity);
        return true;
    }

    public static boolean measureItem(final ItemStack stack, final Player player) {
        if (!(stack.getItem() instanceof IElectricItem electric)) {
            return false;
        }
        player.displayClientMessage(Component.translatable("message.ic2port.eu_reader.item"), true);
        player.displayClientMessage(
                Component.translatable(
                        "message.ic2port.eu_reader.storage",
                        formatEnergy(electric.getStoredEnergy(stack)),
                        formatEnergy(electric.getMaxEnergy())),
                true);
        player.displayClientMessage(
                Component.translatable("message.ic2port.eu_reader.tier", tierLabel(electric.getTier()), formatEnergy(EnergyTier.maxPacketForTier(electric.getTier()))),
                true);
        return true;
    }

    private static void sendNodeStats(final Player player, final IEnergyNode node) {
        player.displayClientMessage(
                Component.translatable(
                        "message.ic2port.eu_reader.tier",
                        tierLabel(node.getTier()),
                        formatEnergy(EnergyTier.maxPacketForTier(node.getTier()))),
                true);
        player.displayClientMessage(
                Component.translatable(
                        "message.ic2port.eu_reader.storage",
                        formatEnergy(node.getStoredEnergy()),
                        formatEnergy(node.getCapacity())),
                true);
        if (node instanceof IEnergyEmitter emitter) {
            double offered = emitter.getOfferedEnergy();
            if (offered > 0.0D) {
                player.displayClientMessage(
                        Component.translatable("message.ic2port.eu_reader.offered", formatEnergy(offered)),
                        true);
            }
        }
    }

    private static void appendCableStats(final Player player, final BlockEntity blockEntity) {
        if (blockEntity instanceof BaseCableBlockEntity cable) {
            appendCableView(player, cable.getDebugStatus().inputDirection(), cable.getTransferLoss());
        }
    }

    private static void appendCableView(
            final Player player,
            final @Nullable Direction inputDirection,
            final double loss) {
        player.displayClientMessage(Component.translatable("message.ic2port.eu_reader.cable_loss", formatEnergy(loss)), true);
        if (inputDirection != null) {
            player.displayClientMessage(
                    Component.translatable(
                            "message.ic2port.eu_reader.cable_input",
                            directionLabel(inputDirection)),
                    true);
        }
    }

    private static Component tierLabel(final int tier) {
        return switch (tier) {
            case EnergyTier.LV -> Component.translatable("message.ic2port.eu_reader.tier.lv");
            case EnergyTier.MV -> Component.translatable("message.ic2port.eu_reader.tier.mv");
            case EnergyTier.HV -> Component.translatable("message.ic2port.eu_reader.tier.hv");
            case EnergyTier.EV -> Component.translatable("message.ic2port.eu_reader.tier.ev");
            default -> Component.translatable("message.ic2port.eu_reader.tier.unknown");
        };
    }

    private static Component directionLabel(final Direction direction) {
        return Component.translatable("direction.minecraft." + direction.getSerializedName());
    }

    public static String formatEnergy(final double amount) {
        if (Math.abs(amount - Math.rint(amount)) < 0.05D) {
            return String.valueOf((long) Math.rint(amount));
        }
        return String.format("%.1f", amount);
    }
}
