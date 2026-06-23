package dev.ic2port.block;

import dev.ic2port.setup.BlockRegistry;
import dev.ic2port.util.TubeConnectionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Connects to rubber logs and inventories to pull sticky resin without drops.
 */
public class StickyTubeBlock extends BaseTubeBlock {

    public StickyTubeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GREEN)
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
        BlockState neighbor = level.getBlockState(tubePos.relative(direction));
        if (neighbor.is(BlockRegistry.RUBBER_WOOD.get())) {
            return true;
        }
        return super.canConnectTo(level, tubePos, state, direction);
    }
}
