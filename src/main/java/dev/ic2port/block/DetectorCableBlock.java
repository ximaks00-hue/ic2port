package dev.ic2port.block;

import dev.ic2port.blockentity.DetectorCableBlockEntity;
import dev.ic2port.setup.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DetectorCableBlock extends CopperCableBlock {

    public DetectorCableBlock(final Properties properties) {
        super(properties);
    }

    @Nullable @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new DetectorCableBlockEntity(pos, state);
    }

    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level, final BlockState state,
                                                                   final BlockEntityType<T> type) {
        return cableTickerOrNull(
                level, type, BlockEntityRegistry.DETECTOR_CABLE_BE.get(), DetectorCableBlockEntity::serverTick);
    }

    @Override
    public boolean hasAnalogOutputSignal(final BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(final BlockState state, final Level level, final BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DetectorCableBlockEntity detector) {
            return detector.getRedstoneStrength();
        }
        return 0;
    }
}
