package dev.ic2port.block;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Fulfills logistic requests from adjacent inventories on the facing side.
 */
public class ProviderTubeBlock extends DirectionalTubeBlock {

    public ProviderTubeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.EMERALD)
                .strength(0.5F)
                .noOcclusion()
                .sound(SoundType.METAL));
    }
}
