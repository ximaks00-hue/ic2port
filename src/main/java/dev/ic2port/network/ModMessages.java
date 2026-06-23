package dev.ic2port.network;

import dev.ic2port.Reference;
import dev.ic2port.network.packet.EnergySyncS2CPacket;
import dev.ic2port.network.packet.ToggleDrillModePacket;
import dev.ic2port.network.packet.ToggleMiningLaserModePacket;
import dev.ic2port.network.packet.VillagerOMatActionPacket;
import dev.ic2port.network.packet.PersonalStorageActionPacket;
import dev.ic2port.network.packet.FluidOMatActionPacket;
import dev.ic2port.network.packet.TradeOMatActionPacket;
import dev.ic2port.network.packet.TradeOMatBuyerViewS2CPacket;
import dev.ic2port.network.packet.ElectricEnchanterActionPacket;
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
        CHANNEL.messageBuilder(ToggleMiningLaserModePacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(ToggleMiningLaserModePacket::encode)
                .decoder(ToggleMiningLaserModePacket::decode)
                .consumerMainThread(ToggleMiningLaserModePacket::handle)
                .add();
        CHANNEL.messageBuilder(ToggleQuantumNightVisionPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(ToggleQuantumNightVisionPacket::encode)
                .decoder(ToggleQuantumNightVisionPacket::decode)
                .consumerMainThread(ToggleQuantumNightVisionPacket::handle)
                .add();
        CHANNEL.messageBuilder(VillagerOMatActionPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(VillagerOMatActionPacket::encode)
                .decoder(VillagerOMatActionPacket::decode)
                .consumerMainThread(VillagerOMatActionPacket::handle)
                .add();
        CHANNEL.messageBuilder(PersonalStorageActionPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(PersonalStorageActionPacket::encode)
                .decoder(PersonalStorageActionPacket::decode)
                .consumerMainThread(PersonalStorageActionPacket::handle)
                .add();
        CHANNEL.messageBuilder(TradeOMatActionPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(TradeOMatActionPacket::encode)
                .decoder(TradeOMatActionPacket::decode)
                .consumerMainThread(TradeOMatActionPacket::handle)
                .add();
        CHANNEL.messageBuilder(FluidOMatActionPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(FluidOMatActionPacket::encode)
                .decoder(FluidOMatActionPacket::decode)
                .consumerMainThread(FluidOMatActionPacket::handle)
                .add();
        CHANNEL.messageBuilder(ElectricEnchanterActionPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(ElectricEnchanterActionPacket::encode)
                .decoder(ElectricEnchanterActionPacket::decode)
                .consumerMainThread(ElectricEnchanterActionPacket::handle)
                .add();
        CHANNEL.messageBuilder(TradeOMatBuyerViewS2CPacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(TradeOMatBuyerViewS2CPacket::encode)
                .decoder(TradeOMatBuyerViewS2CPacket::decode)
                .consumerMainThread(TradeOMatBuyerViewS2CPacket::handle)
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
