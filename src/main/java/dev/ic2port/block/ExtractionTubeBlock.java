package dev.ic2port.block;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Pulls one item per tick interval from the inventory on its facing side.
 */
public class ExtractionTubeBlock extends DirectionalTubeBlock {

    public ExtractionTubeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.GOLD)
                .strength(0.5F)
                .noOcclusion()
                .sound(SoundType.METAL));
    }
}
