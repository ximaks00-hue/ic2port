package dev.ic2port.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ic2port.block.RubberWoodBlock;
import dev.ic2port.setup.BlockRegistry;
import dev.ic2port.setup.ModTreeDecoratorTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

/**
 * Marks random rubber logs with resin during tree generation.
 */
public class RubberResinDecorator extends TreeDecorator {

    public static final Codec<RubberResinDecorator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.floatRange(0.0F, 1.0F)
                            .optionalFieldOf("probability", 0.35F)
                            .forGetter(decorator -> decorator.probability))
            .apply(instance, RubberResinDecorator::new));

    private final float probability;

    public RubberResinDecorator(final float probability) {
        this.probability = probability;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return ModTreeDecoratorTypes.RUBBER_RESIN.get();
    }

    @Override
    public void place(final Context context) {
        RandomSource random = context.random();
        for (BlockPos pos : context.logs()) {
            if (random.nextFloat() >= probability) {
                continue;
            }
            final BlockState[] current = new BlockState[1];
            if (!context.level().isStateAtPosition(
                    pos,
                    candidate -> {
                        if (candidate.is(BlockRegistry.RUBBER_WOOD.get())
                                && candidate.hasProperty(RubberWoodBlock.RESIN)
                                && !candidate.getValue(RubberWoodBlock.RESIN)
                                && !candidate.getValue(RubberWoodBlock.DEPLETED)) {
                            current[0] = candidate;
                            return true;
                        }
                        return false;
                    })) {
                continue;
            }
            context.setBlock(pos, current[0].setValue(RubberWoodBlock.RESIN, true));
        }
    }
}
