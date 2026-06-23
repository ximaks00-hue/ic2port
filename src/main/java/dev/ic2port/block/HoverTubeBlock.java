package dev.ic2port.block;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Items travelling through ignore gravity-based speed changes.
 */
public class HoverTubeBlock extends BaseTubeBlock {

    public HoverTubeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.QUARTZ)
                .strength(0.5F)
                .noOcclusion()
                .sound(SoundType.METAL));
    }
}
