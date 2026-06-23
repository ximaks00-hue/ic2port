package dev.ic2port.network.packet;

import dev.ic2port.api.tiles.IPersonalStorage;
import dev.ic2port.network.ModMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Owner actions for personal storage friends ACL.
 */
public final class PersonalStorageActionPacket {

    public static final byte ACTION_ADD_NEAREST = 0;
    public static final byte ACTION_REMOVE_LAST = 1;

    private final BlockPos pos;
    private final byte action;

    public PersonalStorageActionPacket(final BlockPos pos, final byte action) {
        this.pos = pos;
        this.action = action;
    }

    public PersonalStorageActionPacket(final FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.action = buf.readByte();
    }

    public static void encode(final PersonalStorageActionPacket packet, final FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeByte(packet.action);
    }

    public static PersonalStorageActionPacket decode(final FriendlyByteBuf buf) {
        return new PersonalStorageActionPacket(buf);
    }

    public static void handle(
            final PersonalStorageActionPacket packet,
            final Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            ServerPlayer player = contextSupplier.get().getSender();
            if (player == null) {
                return;
            }
            BlockEntity blockEntity = player.level().getBlockEntity(packet.pos);
            if (!(blockEntity instanceof IPersonalStorage storage)) {
                return;
            }
            if (!storage.canAccess(player)) {
                return;
            }
            if (storage.getOwnerUuid() != null && !storage.getOwnerUuid().equals(player.getUUID())) {
                return;
            }
            if (player.distanceToSqr(
                    packet.pos.getX() + 0.5D,
                    packet.pos.getY() + 0.5D,
                    packet.pos.getZ() + 0.5D) > 64.0D) {
                return;
            }
            switch (packet.action) {
                case ACTION_ADD_NEAREST -> {
                    ServerPlayer nearest = player.server.getPlayerList().getPlayers().stream()
                            .filter(candidate -> candidate != player
                                    && candidate.level() == player.level()
                                    && candidate.distanceToSqr(packet.pos.getX() + 0.5D,
                                    packet.pos.getY() + 0.5D,
                                    packet.pos.getZ() + 0.5D) <= 64.0D)
                            .min((left, right) -> Double.compare(
                                    left.distanceToSqr(packet.pos.getX() + 0.5D,
                                            packet.pos.getY() + 0.5D,
                                            packet.pos.getZ() + 0.5D),
                                    right.distanceToSqr(packet.pos.getX() + 0.5D,
                                            packet.pos.getY() + 0.5D,
                                            packet.pos.getZ() + 0.5D)))
                            .orElse(null);
                    if (nearest != null) {
                        storage.addFriend(nearest.getUUID());
                    }
                }
                case ACTION_REMOVE_LAST -> {
                    if (!storage.getFriends().isEmpty()) {
                        storage.removeFriend(storage.getFriends().get(storage.getFriends().size() - 1));
                    }
                }
                default -> {
                }
            }
            blockEntity.setChanged();
        });
        contextSupplier.get().setPacketHandled(true);
    }

    public static void send(final BlockPos pos, final byte action) {
        ModMessages.sendToServer(new PersonalStorageActionPacket(pos, action));
    }
}
