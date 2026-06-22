package dev.ic2port.setup;

import dev.ic2port.Reference;
import dev.ic2port.item.NanoSuitItem;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class NanoSuitForgeEvents {

    private NanoSuitForgeEvents() {
        throw new UnsupportedOperationException("Utility class");
    }

    @SubscribeEvent
    public static void onLivingHurt(final LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }
        if (!NanoSuitItem.hasFullSet(player)) {
            return;
        }

        float remaining;
        if (event.getSource().is(DamageTypes.FALL)) {
            remaining = NanoSuitItem.absorbFallDamage(player, event.getAmount());
        } else {
            remaining = NanoSuitItem.absorbDamage(player, event.getAmount());
        }

        if (remaining < event.getAmount()) {
            event.setAmount(remaining);
        }
    }
}
