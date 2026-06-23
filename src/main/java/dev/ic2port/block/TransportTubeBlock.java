package dev.ic2port.block;

import dev.ic2port.util.TubeConnectionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Connects only to other tubes — inventories are ignored.
 */
public class TransportTubeBlock extends BaseTubeBlock {

    public TransportTubeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(0.5F)
                .noOcclusion()
                .sound(SoundType.METAL));
    }

    @Override
    protected boolean canConnectTo(
            final LevelAccessor level,
            final BlockPos tubePos,
            final BlockState state,
            final Direction direction) {
        return TubeConnectionHelper.isTubeBlock(level.getBlockState(tubePos.relative(direction)));
    }
}
