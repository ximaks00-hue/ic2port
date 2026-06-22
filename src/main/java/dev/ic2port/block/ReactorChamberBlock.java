package dev.ic2port.block;

import dev.ic2port.blockentity.NuclearReactorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Expands the adjacent nuclear reactor grid by one column per attached chamber.
 */
public class ReactorChamberBlock extends Block {

    public ReactorChamberBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 6.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL));
    }

    @Override
    public void onPlace(final BlockState state, final Level level, final BlockPos pos, final BlockState oldState, final boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        notifyAdjacentReactors(level, pos);
    }

    @Override
    public void onRemove(final BlockState state, final Level level, final BlockPos pos, final BlockState newState, final boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        if (!state.is(newState.getBlock())) {
            notifyAdjacentReactors(level, pos);
        }
    }

    private static void notifyAdjacentReactors(final Level level, final BlockPos pos) {
        if (level.isClientSide) {
            return;
        }
        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            if (level.getBlockEntity(neighborPos) instanceof NuclearReactorBlockEntity reactor) {
                reactor.refreshChamberCount();
            }
        }
    }
}
