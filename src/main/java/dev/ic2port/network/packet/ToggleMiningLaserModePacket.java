package dev.ic2port.network.packet;

import dev.ic2port.item.MiningLaserItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Cycles the mode of a {@link MiningLaserItem} held by the sending player.
 */
public record ToggleMiningLaserModePacket() {

    public static void encode(final ToggleMiningLaserModePacket packet, final FriendlyByteBuf buffer) {
    }

    public static ToggleMiningLaserModePacket decode(final FriendlyByteBuf buffer) {
        return new ToggleMiningLaserModePacket();
    }

    public static void handle(
            final ToggleMiningLaserModePacket packet,
            final Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            ItemStack laserStack = MiningLaserItem.findHeldLaser(player);
            if (laserStack == null || !(laserStack.getItem() instanceof MiningLaserItem laser)) {
                return;
            }
            laser.cycleMode(laserStack);
            MiningLaserItem.notifyModeSwitch(player, laserStack);
        });
        context.setPacketHandled(true);
    }
}
