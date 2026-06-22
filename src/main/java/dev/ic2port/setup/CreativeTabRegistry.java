package dev.ic2port.setup;

import dev.ic2port.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Creative mode tab registration for mod content.
 * <p>
 * Uses a vanilla placeholder icon until mod items are registered.
 */
public final class CreativeTabRegistry {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Reference.MOD_ID);

    public static final RegistryObject<CreativeModeTab> IC2_TAB = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + Reference.MOD_ID))
                    .icon(() -> new ItemStack(Items.IRON_INGOT))
                    .displayItems((parameters, output) -> {
                        // Populated automatically when items declare this tab as their creative tab.
                    })
                    .build());

    private CreativeTabRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void register(final IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
