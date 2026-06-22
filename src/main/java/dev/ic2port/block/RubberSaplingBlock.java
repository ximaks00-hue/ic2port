package dev.ic2port.block;

import dev.ic2port.setup.ModConfiguredFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

/**
 * Rubber tree sapling — grows into the configured {@code ic2port:rubber_tree} feature.
 */
public class RubberSaplingBlock extends SaplingBlock {

    private static final AbstractTreeGrower RUBBER_TREE_GROWER = new AbstractTreeGrower() {
        @Override
        protected @Nullable ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(
                final RandomSource random,
                final boolean largeHive) {
            return ModConfiguredFeatures.RUBBER_TREE;
        }
    };

    public RubberSaplingBlock(final Properties properties) {
        super(RUBBER_TREE_GROWER, properties);
    }
}
