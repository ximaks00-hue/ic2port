package dev.ic2port.network;

import dev.ic2port.Reference;
import dev.ic2port.network.packet.EnergySyncS2CPacket;
import dev.ic2port.network.packet.ToggleDrillModePacket;
import dev.ic2port.network.packet.ToggleQuantumNightVisionPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Central networking channel for IC2 Port client–server packets.
 */
public final class ModMessages {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Reference.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId;

    private ModMessages() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void register() {
        CHANNEL.messageBuilder(EnergySyncS2CPacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(EnergySyncS2CPacket::encode)
                .decoder(EnergySyncS2CPacket::decode)
                .consumerMainThread(EnergySyncS2CPacket::handle)
                .add();
        CHANNEL.messageBuilder(ToggleDrillModePacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(ToggleDrillModePacket::encode)
                .decoder(ToggleDrillModePacket::decode)
                .consumerMainThread(ToggleDrillModePacket::handle)
                .add();
        CHANNEL.messageBuilder(ToggleQuantumNightVisionPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(ToggleQuantumNightVisionPacket::encode)
                .decoder(ToggleQuantumNightVisionPacket::decode)
                .consumerMainThread(ToggleQuantumNightVisionPacket::handle)
                .add();
    }

    public static void sendToServer(final Object packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void sendToClientsTrackingChunk(final Object packet, final ServerLevel level, final BlockPos pos) {
        CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), packet);
    }

    private static int nextId() {
        return packetId++;
    }
}
