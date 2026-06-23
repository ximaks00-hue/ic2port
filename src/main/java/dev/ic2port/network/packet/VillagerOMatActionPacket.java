package dev.ic2port.network.packet;

import dev.ic2port.blockentity.VillagerOMatBlockEntity;
import dev.ic2port.network.ModMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client actions for the Villager-O-Mat GUI.
 */
public final class VillagerOMatActionPacket {

    public static final byte ACTION_PREV_VILLAGER = 0;
    public static final byte ACTION_NEXT_VILLAGER = 1;
    public static final byte ACTION_TOGGLE_TRADE = 2;
    public static final byte ACTION_COLLECT_XP = 3;

    private final BlockPos pos;
    private final byte action;
    private final int parameter;

    public VillagerOMatActionPacket(final BlockPos pos, final byte action, final int parameter) {
        this.pos = pos;
        this.action = action;
        this.parameter = parameter;
    }

    public VillagerOMatActionPacket(final FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.action = buf.readByte();
        this.parameter = buf.readVarInt();
    }

    public static void encode(final VillagerOMatActionPacket packet, final FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeByte(packet.action);
        buf.writeVarInt(packet.parameter);
    }

    public static VillagerOMatActionPacket decode(final FriendlyByteBuf buf) {
        return new VillagerOMatActionPacket(buf);
    }

    public static void handle(final VillagerOMatActionPacket packet, final Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) {
                return;
            }
            BlockEntity blockEntity = player.level().getBlockEntity(packet.pos);
            if (!(blockEntity instanceof VillagerOMatBlockEntity mat)) {
                return;
            }
            if (player.distanceToSqr(packet.pos.getX() + 0.5D, packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D) > 64.0D) {
                return;
            }
            switch (packet.action) {
                case ACTION_PREV_VILLAGER -> mat.cycleVillager(-1);
                case ACTION_NEXT_VILLAGER -> mat.cycleVillager(1);
                case ACTION_TOGGLE_TRADE -> mat.toggleTrade(packet.parameter);
                case ACTION_COLLECT_XP -> mat.collectXp(player);
                default -> {
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void send(final BlockPos pos, final byte action, final int parameter) {
        ModMessages.sendToServer(new VillagerOMatActionPacket(pos, action, parameter));
    }
}
