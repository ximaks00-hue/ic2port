package dev.ic2port.setup;

import dev.ic2port.Reference;
import dev.ic2port.energy.WorldEnergyNet;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Drives the global EU energy net once per server level tick.
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class EnergyNetForgeEvents {

    private EnergyNetForgeEvents() {
        throw new UnsupportedOperationException("Utility class");
    }

    @SubscribeEvent
    public static void onLevelTick(final TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || event.level.isClientSide()
                || !WorldEnergyNet.isEnabled()) {
            return;
        }
        if (event.level instanceof ServerLevel serverLevel) {
            WorldEnergyNet.get(serverLevel).tick();
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(final LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            WorldEnergyNet.removeLevel(serverLevel.dimension());
        }
    }
}
