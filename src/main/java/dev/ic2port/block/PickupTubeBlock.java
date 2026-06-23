package dev.ic2port.block;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Picks up item entities from the ground and routes them into the tube network.
 */
public class PickupTubeBlock extends BaseTubeBlock {

    public PickupTubeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.EMERALD)
                .strength(0.5F)
                .noOcclusion()
                .sound(SoundType.METAL));
    }
}
