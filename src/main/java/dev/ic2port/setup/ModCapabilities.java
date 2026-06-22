package dev.ic2port.setup;

import dev.ic2port.Reference;
import dev.ic2port.api.energy.IEnergyNode;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge capability registration for the IC2 Port EU network API.
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModCapabilities {

    public static final Capability<IEnergyNode> ENERGY_NODE_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {});

    private ModCapabilities() {
        throw new UnsupportedOperationException("Utility class");
    }

    @SubscribeEvent
    public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.register(IEnergyNode.class);
    }
}
