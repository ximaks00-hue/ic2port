package dev.ic2port.setup;

import dev.ic2port.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

/**
 * Resource keys for JSON-defined configured features (sapling growth, etc.).
 */
public final class ModConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> RUBBER_TREE =
            ResourceKey.create(
                    Registries.CONFIGURED_FEATURE,
                    new ResourceLocation(Reference.MOD_ID, "rubber_tree"));

    private ModConfiguredFeatures() {
        throw new UnsupportedOperationException("Utility class");
    }
}
