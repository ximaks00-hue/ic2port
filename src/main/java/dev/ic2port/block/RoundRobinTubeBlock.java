package dev.ic2port.block;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Sends each item to the next connected side in strict rotation.
 */
public class RoundRobinTubeBlock extends BaseTubeBlock {

    public RoundRobinTubeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_CYAN)
                .strength(0.5F)
                .noOcclusion()
                .sound(SoundType.METAL));
    }
}
