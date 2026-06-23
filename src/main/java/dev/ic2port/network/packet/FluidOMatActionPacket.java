package dev.ic2port.network.packet;

import dev.ic2port.blockentity.FluidOMatBlockEntity;
import dev.ic2port.network.ModMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Owner actions for the Fluid-O-Mat GUI (price adjustment).
 */
public final class FluidOMatActionPacket {

    public static final byte ACTION_SET_PRICE = 0;
    public static final byte ACTION_ADJUST_PRICE = 1;

    private final BlockPos pos;
    private final byte action;
    private final int parameter;

    public FluidOMatActionPacket(final BlockPos pos, final byte action, final int parameter) {
        this.pos = pos;
        this.action = action;
        this.parameter = parameter;
    }

    public FluidOMatActionPacket(final FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.action = buf.readByte();
        this.parameter = buf.readVarInt();
    }

    public static void encode(final FluidOMatActionPacket packet, final FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeByte(packet.action);
        buf.writeVarInt(packet.parameter);
    }

    public static FluidOMatActionPacket decode(final FriendlyByteBuf buf) {
        return new FluidOMatActionPacket(buf);
    }

    public static void handle(final FluidOMatActionPacket packet, final Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) {
                return;
            }
            BlockEntity blockEntity = player.level().getBlockEntity(packet.pos);
            if (!(blockEntity instanceof FluidOMatBlockEntity mat)) {
                return;
            }
            if (player.distanceToSqr(
                    packet.pos.getX() + 0.5D,
                    packet.pos.getY() + 0.5D,
                    packet.pos.getZ() + 0.5D) > 64.0D) {
                return;
            }
            if (!mat.isOwner(player)) {
                return;
            }
            switch (packet.action) {
                case ACTION_SET_PRICE -> mat.setPriceCoins(packet.parameter);
                case ACTION_ADJUST_PRICE -> mat.setPriceCoins(mat.getPriceCoins() + packet.parameter);
                default -> {
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void send(final BlockPos pos, final byte action, final int parameter) {
        ModMessages.sendToServer(new FluidOMatActionPacket(pos, action, parameter));
    }
}
