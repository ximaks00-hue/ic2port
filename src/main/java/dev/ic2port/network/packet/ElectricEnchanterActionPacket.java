package dev.ic2port.network.packet;

import dev.ic2port.blockentity.ElectricEnchanterBlockEntity;
import dev.ic2port.network.ModMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client actions for the Electric Enchanter GUI.
 */
public final class ElectricEnchanterActionPacket {

    public static final byte ACTION_SET_TAB = 0;
    public static final byte ACTION_ENCHANT = 1;
    public static final byte ACTION_STORE_XP = 2;

    private final BlockPos pos;
    private final byte action;
    private final int parameter;

    public ElectricEnchanterActionPacket(final BlockPos pos, final byte action, final int parameter) {
        this.pos = pos;
        this.action = action;
        this.parameter = parameter;
    }

    public ElectricEnchanterActionPacket(final FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.action = buf.readByte();
        this.parameter = buf.readVarInt();
    }

    public static void encode(final ElectricEnchanterActionPacket packet, final FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeByte(packet.action);
        buf.writeVarInt(packet.parameter);
    }

    public static ElectricEnchanterActionPacket decode(final FriendlyByteBuf buf) {
        return new ElectricEnchanterActionPacket(buf);
    }

    public static void handle(final ElectricEnchanterActionPacket packet, final Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) {
                return;
            }
            BlockEntity blockEntity = player.level().getBlockEntity(packet.pos);
            if (!(blockEntity instanceof ElectricEnchanterBlockEntity enchanter)) {
                return;
            }
            if (player.distanceToSqr(packet.pos.getX() + 0.5D, packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D) > 64.0D) {
                return;
            }
            switch (packet.action) {
                case ACTION_SET_TAB -> enchanter.setActiveTab(packet.parameter);
                case ACTION_ENCHANT -> enchanter.tryEnchant(player);
                case ACTION_STORE_XP -> enchanter.storePlayerXp(player, packet.parameter);
                default -> {
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void send(final BlockPos pos, final byte action, final int parameter) {
        ModMessages.sendToServer(new ElectricEnchanterActionPacket(pos, action, parameter));
    }
}
