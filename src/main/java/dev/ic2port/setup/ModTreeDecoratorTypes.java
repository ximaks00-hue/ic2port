package dev.ic2port.setup;

import dev.ic2port.Reference;
import dev.ic2port.worldgen.RubberResinDecorator;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModTreeDecoratorTypes {

    public static final DeferredRegister<TreeDecoratorType<?>> DECORATORS =
            DeferredRegister.create(ForgeRegistries.TREE_DECORATOR_TYPES, Reference.MOD_ID);

    public static final RegistryObject<TreeDecoratorType<RubberResinDecorator>> RUBBER_RESIN =
            DECORATORS.register("rubber_resin", () -> new TreeDecoratorType<>(RubberResinDecorator.CODEC));

    private ModTreeDecoratorTypes() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void register(final IEventBus modEventBus) {
        DECORATORS.register(modEventBus);
    }
}
