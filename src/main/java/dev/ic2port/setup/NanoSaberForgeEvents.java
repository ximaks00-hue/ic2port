package dev.ic2port.setup;

import dev.ic2port.Reference;
import dev.ic2port.item.NanoSaberItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class NanoSaberForgeEvents {

    private NanoSaberForgeEvents() {
        throw new UnsupportedOperationException("Utility class");
    }

    @SubscribeEvent
    public static void onLivingHurt(final LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof NanoSaberItem saber) || !saber.isActive(stack)) {
            return;
        }
        if (saber.getStoredEnergy(stack) < NanoSaberItem.EU_PER_HIT) {
            return;
        }

        event.setAmount(NanoSaberItem.resolveActiveDamage(event.getEntity()));
    }
}
