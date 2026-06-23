package dev.ic2port.block;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Accelerates items passing through the tube network.
 */
public class SpeedTubeBlock extends BaseTubeBlock {

    public SpeedTubeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_RED)
                .strength(0.5F)
                .noOcclusion()
                .sound(SoundType.METAL));
    }
}
