package dev.ic2port.setup;

import dev.ic2port.Reference;
import dev.ic2port.effect.RadiationEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Reference.MOD_ID);

    public static final RegistryObject<MobEffect> RADIATION =
            MOB_EFFECTS.register("radiation", RadiationEffect::new);

    private ModEffects() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void register(final IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
    }
}
