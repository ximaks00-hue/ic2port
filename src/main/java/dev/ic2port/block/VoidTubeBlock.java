package dev.ic2port.block;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Destroys any item that enters this tube segment.
 */
public class VoidTubeBlock extends BaseTubeBlock {

    public VoidTubeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BLACK)
                .strength(0.5F)
                .noOcclusion()
                .sound(SoundType.METAL));
    }
}
