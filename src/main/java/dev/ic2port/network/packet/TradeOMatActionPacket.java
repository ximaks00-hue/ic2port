package dev.ic2port.network.packet;

import dev.ic2port.blockentity.TradeOMatBlockEntity;
import dev.ic2port.network.ModMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client actions for the Trade-O-Mat GUI.
 */
public final class TradeOMatActionPacket {

    public static final byte ACTION_SET_PRICE = 0;
    public static final byte ACTION_TOGGLE_BUYER_VIEW = 1;

    private final BlockPos pos;
    private final byte action;
    private final int parameter;

    public TradeOMatActionPacket(final BlockPos pos, final byte action, final int parameter) {
        this.pos = pos;
        this.action = action;
        this.parameter = parameter;
    }

    public TradeOMatActionPacket(final FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.action = buf.readByte();
        this.parameter = buf.readVarInt();
    }

    public static void encode(final TradeOMatActionPacket packet, final FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeByte(packet.action);
        buf.writeVarInt(packet.parameter);
    }

    public static TradeOMatActionPacket decode(final FriendlyByteBuf buf) {
        return new TradeOMatActionPacket(buf);
    }

    public static void handle(final TradeOMatActionPacket packet, final Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) {
                return;
            }
            BlockEntity blockEntity = player.level().getBlockEntity(packet.pos);
            if (!(blockEntity instanceof TradeOMatBlockEntity mat)) {
                return;
            }
            if (player.distanceToSqr(packet.pos.getX() + 0.5D, packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D) > 64.0D) {
                return;
            }
            switch (packet.action) {
                case ACTION_SET_PRICE -> {
                    if (mat.isOwner(player)) {
                        mat.setPriceCoins(packet.parameter);
                    }
                }
                case ACTION_TOGGLE_BUYER_VIEW -> mat.toggleBuyerView(player);
                default -> {
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void send(final BlockPos pos, final byte action, final int parameter) {
        ModMessages.sendToServer(new TradeOMatActionPacket(pos, action, parameter));
    }
}
