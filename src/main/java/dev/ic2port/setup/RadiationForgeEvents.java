package dev.ic2port.setup;

import dev.ic2port.Reference;
import dev.ic2port.item.HazmatArmorItem;
import dev.ic2port.item.NanoSuitItem;
import dev.ic2port.item.QuantumSuitItem;
import dev.ic2port.setup.ModEffects;
import dev.ic2port.util.RadiationHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class RadiationForgeEvents {

    private static final int RADIATION_DURATION_TICKS = 220;
    private static final int RADIATION_AMPLIFIER = 0;
    private static final int ORE_SCAN_INTERVAL = 20;

    private RadiationForgeEvents() {
        throw new UnsupportedOperationException("Utility class");
    }

    @SubscribeEvent
    public static void onPlayerTick(final TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !event.side.isServer()) {
            return;
        }

        Player player = event.player;
        if (HazmatArmorItem.hasFullSet(player)
                || NanoSuitItem.hasFullSet(player)
                || QuantumSuitItem.hasFullSet(player)) {
            player.removeEffect(ModEffects.RADIATION.get());
            return;
        }

        if (!RadiationHelper.isExposedToRadiation(player, ORE_SCAN_INTERVAL)) {
            return;
        }

        player.addEffect(new MobEffectInstance(
                ModEffects.RADIATION.get(),
                RADIATION_DURATION_TICKS,
                RADIATION_AMPLIFIER,
                false,
                true,
                true));
    }
}
