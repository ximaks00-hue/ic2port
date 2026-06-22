package dev.ic2port.setup;

import dev.ic2port.Reference;
import dev.ic2port.util.PortableEnergyPackHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class PortableEnergyPackForgeEvents {

    private PortableEnergyPackForgeEvents() {
        throw new UnsupportedOperationException("Utility class");
    }

    @SubscribeEvent
    public static void onPlayerTick(final TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !event.side.isServer()) {
            return;
        }

        Player player = event.player;
        PortableEnergyPackHelper.tickChestPack(player);
        PortableEnergyPackHelper.tickInstalledModules(player);
    }
}
