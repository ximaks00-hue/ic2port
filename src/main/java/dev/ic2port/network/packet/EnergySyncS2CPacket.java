package dev.ic2port.network.packet;

import dev.ic2port.blockentity.BaseMachineBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server-to-client packet that synchronizes stored EU for a machine block entity.
 */
public record EnergySyncS2CPacket(BlockPos pos, double energy) {

    public static void encode(final EnergySyncS2CPacket packet, final FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeDouble(packet.energy);
    }

    public static EnergySyncS2CPacket decode(final FriendlyByteBuf buffer) {
        return new EnergySyncS2CPacket(buffer.readBlockPos(), buffer.readDouble());
    }

    public static void handle(final EnergySyncS2CPacket packet, final Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level == null) {
                return;
            }
            if (Minecraft.getInstance().level.getBlockEntity(packet.pos()) instanceof BaseMachineBlockEntity machine) {
                machine.setClientStoredEnergy(packet.energy());
            }
        });
        context.setPacketHandled(true);
    }
}
