package dev.ic2port.network.packet;

import dev.ic2port.item.QuantumSuitItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Toggles night vision on the player's quantum helmet.
 */
public record ToggleQuantumNightVisionPacket() {

    public static void encode(final ToggleQuantumNightVisionPacket packet, final FriendlyByteBuf buffer) {
    }

    public static ToggleQuantumNightVisionPacket decode(final FriendlyByteBuf buffer) {
        return new ToggleQuantumNightVisionPacket();
    }

    public static void handle(
            final ToggleQuantumNightVisionPacket packet,
            final Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            if (!(helmet.getItem() instanceof QuantumSuitItem quantum)
                    || quantum.getType() != QuantumSuitItem.Type.HELMET) {
                return;
            }
            boolean enabled = quantum.toggleNightVision(helmet);
            player.displayClientMessage(
                    Component.translatable(
                                    enabled
                                            ? "item.ic2port.quantum_helmet.night_vision.on"
                                            : "item.ic2port.quantum_helmet.night_vision.off")
                            .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.GRAY),
                    true);
        });
        context.setPacketHandled(true);
    }
}
