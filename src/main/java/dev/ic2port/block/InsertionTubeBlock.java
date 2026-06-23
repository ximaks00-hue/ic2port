package dev.ic2port.block;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Prefers inserting items into inventories over forwarding them to other tubes.
 */
public class InsertionTubeBlock extends BaseTubeBlock {

    public InsertionTubeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_LIGHT_BLUE)
                .strength(0.5F)
                .noOcclusion()
                .sound(SoundType.METAL));
    }
}
