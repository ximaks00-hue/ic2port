package dev.ic2port.network.packet;

import dev.ic2port.menu.TradeOMatMenu;
import dev.ic2port.network.ModMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Syncs buyer-view mode for the Trade-O-Mat GUI.
 */
public record TradeOMatBuyerViewS2CPacket(BlockPos pos, boolean buyerView) {

    public static void encode(final TradeOMatBuyerViewS2CPacket packet, final FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeBoolean(packet.buyerView);
    }

    public static TradeOMatBuyerViewS2CPacket decode(final FriendlyByteBuf buf) {
        return new TradeOMatBuyerViewS2CPacket(buf.readBlockPos(), buf.readBoolean());
    }

    public static void handle(
            final TradeOMatBuyerViewS2CPacket packet,
            final Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            if (Minecraft.getInstance().player == null) {
                return;
            }
            if (Minecraft.getInstance().player.containerMenu instanceof TradeOMatMenu menu) {
                menu.setBuyerView(packet.buyerView());
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }

    public static void send(final net.minecraft.server.level.ServerPlayer player, final BlockPos pos, final boolean buyerView) {
        ModMessages.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                new TradeOMatBuyerViewS2CPacket(pos, buyerView));
    }
}
