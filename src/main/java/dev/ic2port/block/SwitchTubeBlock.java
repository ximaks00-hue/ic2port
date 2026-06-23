package dev.ic2port.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Cuts all tube connections while receiving a redstone signal.
 */
public class SwitchTubeBlock extends BaseTubeBlock {

    public SwitchTubeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_RED)
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
        if (level instanceof Level world && world.hasNeighborSignal(tubePos)) {
            return false;
        }
        return super.canConnectTo(level, tubePos, state, direction);
    }

    @Override
    public void neighborChanged(
            final BlockState state,
            final Level level,
            final BlockPos pos,
            final Block block,
            final BlockPos fromPos,
            final boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide) {
            BlockState updated = updateConnections(state, level, pos);
            if (updated != state) {
                level.setBlockAndUpdate(pos, updated);
            }
        }
    }
}
