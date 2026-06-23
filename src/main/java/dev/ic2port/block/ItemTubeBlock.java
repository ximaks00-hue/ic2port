package dev.ic2port.block;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class ItemTubeBlock extends BaseTubeBlock {

    public ItemTubeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(0.5F)
                .noOcclusion()
                .sound(SoundType.METAL));
    }
}
