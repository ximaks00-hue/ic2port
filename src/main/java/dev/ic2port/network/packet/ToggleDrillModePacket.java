package dev.ic2port.network.packet;

import dev.ic2port.item.AdvancedDrillItem;
import dev.ic2port.item.DiamondDrillItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Cycles the mode of an electric drill held by the sending player.
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

            ItemStack advancedStack = AdvancedDrillItem.findHeldDrill(player);
            if (advancedStack != null && advancedStack.getItem() instanceof AdvancedDrillItem advancedDrill) {
                advancedDrill.cycleMode(advancedStack);
                AdvancedDrillItem.notifyModeSwitch(player, advancedStack);
                return;
            }

            ItemStack main = player.getMainHandItem();
            ItemStack off = player.getOffhandItem();
            ItemStack diamondStack = main.getItem() instanceof DiamondDrillItem ? main
                    : off.getItem() instanceof DiamondDrillItem ? off : null;
            if (diamondStack != null && diamondStack.getItem() instanceof DiamondDrillItem diamondDrill) {
                DiamondDrillItem.DrillMode next = diamondDrill.getMode(diamondStack).next();
                diamondDrill.setMode(diamondStack, next);
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(
                                        "item.ic2port.diamond_drill.mode_switch",
                                        net.minecraft.network.chat.Component.translatable(next.getTranslationKey()))
                                .withStyle(next.getChatColor()),
                        true);
            }
        });
        context.setPacketHandled(true);
    }
}
