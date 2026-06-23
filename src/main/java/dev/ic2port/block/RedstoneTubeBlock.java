package dev.ic2port.block;

import dev.ic2port.blockentity.TubeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Emits a redstone signal while items are travelling through the tube.
 */
public class RedstoneTubeBlock extends BaseTubeBlock {

    public RedstoneTubeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_RED)
                .strength(0.5F)
                .noOcclusion()
                .sound(SoundType.METAL));
    }

    @Override
    public boolean isSignalSource(final BlockState state) {
        return true;
    }

    @Override
    public int getSignal(
            final BlockState state,
            final BlockGetter level,
            final BlockPos pos,
            final Direction side) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TubeBlockEntity tube) {
            return tube.getRedstoneTubeSignal();
        }
        return 0;
    }

    @Override
    public int getDirectSignal(
            final BlockState state,
            final BlockGetter level,
            final BlockPos pos,
            final Direction side) {
        return getSignal(state, level, pos, side);
    }
}
