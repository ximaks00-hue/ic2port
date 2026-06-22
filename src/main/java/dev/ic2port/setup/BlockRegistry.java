/**
 * Deferred registry managers and mod bootstrap wiring.
 */
package dev.ic2port.setup;

import dev.ic2port.Reference;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Central registry for all {@link Block} instances of this mod.
 * <p>
 * Add new blocks via {@code public static final RegistryObject<Block> EXAMPLE = register(...)}.
 */
public final class BlockRegistry {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.MOD_ID);

    private BlockRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void register(final IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
