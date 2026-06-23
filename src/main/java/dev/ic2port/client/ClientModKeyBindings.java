package dev.ic2port.client;

import dev.ic2port.Reference;
import dev.ic2port.network.ModMessages;
import dev.ic2port.network.packet.ToggleDrillModePacket;
import dev.ic2port.network.packet.ToggleMiningLaserModePacket;
import dev.ic2port.network.packet.ToggleQuantumNightVisionPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModKeyBindings {

    private ClientModKeyBindings() {
        throw new UnsupportedOperationException("Utility class");
    }

    @SubscribeEvent
    public static void registerKeyMappings(final RegisterKeyMappingsEvent event) {
        event.register(ClientKeyBindings.TOGGLE_ADVANCED_DRILL_MODE);
        event.register(ClientKeyBindings.TOGGLE_QUANTUM_NIGHT_VISION);
        event.register(ClientKeyBindings.TOGGLE_MINING_LASER_MODE);
    }
}

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
final class ClientForgeInputEvents {

    private ClientForgeInputEvents() {
        throw new UnsupportedOperationException("Utility class");
    }

    @SubscribeEvent
    public static void onKeyInput(final InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }
        if (ClientKeyBindings.TOGGLE_ADVANCED_DRILL_MODE.consumeClick()) {
            ModMessages.sendToServer(new ToggleDrillModePacket());
        }
        if (ClientKeyBindings.TOGGLE_MINING_LASER_MODE.consumeClick()) {
            ModMessages.sendToServer(new ToggleMiningLaserModePacket());
        }
        if (ClientKeyBindings.TOGGLE_QUANTUM_NIGHT_VISION.consumeClick()) {
            ModMessages.sendToServer(new ToggleQuantumNightVisionPacket());
        }
    }
}
