package dev.ic2port.network.packet;

import dev.ic2port.item.AdvancedDrillItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Cycles the mode of an {@link AdvancedDrillItem} held by the sending player.
 */
public record ToggleDrillModePacket() {

    public static void encode(final ToggleDrillModePacket packet, final FriendlyByteBuf buffer) {
    }

    public static ToggleDrillModePacket decode(final FriendlyByteBuf buffer) {
        return new ToggleDrillModePacket();
    }

    public static void handle(final ToggleDrillModePacket packet, final Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            ItemStack drillStack = AdvancedDrillItem.findHeldDrill(player);
            if (drillStack == null || !(drillStack.getItem() instanceof AdvancedDrillItem drill)) {
                return;
            }
            drill.cycleMode(drillStack);
            AdvancedDrillItem.notifyModeSwitch(player, drillStack);
        });
        context.setPacketHandled(true);
    }
}
