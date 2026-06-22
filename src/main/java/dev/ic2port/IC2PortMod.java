package dev.ic2port;

import com.mojang.logging.LogUtils;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.BlockRegistry;
import dev.ic2port.setup.CreativeTabRegistry;
import dev.ic2port.setup.ItemRegistry;
import dev.ic2port.setup.MenuTypeRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Main entry point for the IC2 Port mod.
 * <p>
 * Responsible for wiring deferred registries and logging successful initialization.
 * No game content is registered at this stage — only infrastructure.
 */
@Mod(Reference.MOD_ID)
public class IC2PortMod {

    private static final Logger LOGGER = LogUtils.getLogger();

    public IC2PortMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        BlockRegistry.register(modEventBus);
        ItemRegistry.register(modEventBus);
        BlockEntityRegistry.register(modEventBus);
        MenuTypeRegistry.register(modEventBus);
        CreativeTabRegistry.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("[{}] v{} initialized successfully.", Reference.MOD_NAME, Reference.VERSION);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.debug("[{}] Common setup phase started.", Reference.MOD_ID);
    }
}
