package dev.ic2port.block;

import dev.ic2port.blockentity.VoidPipeBlockEntity;
import dev.ic2port.setup.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class VoidPipeBlock extends BaseEntityBlock {

    public VoidPipeBlock(final Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new VoidPipeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            final Level level,
            final BlockState state,
            final BlockEntityType<T> type) {
        return level.isClientSide()
                ? null
                : createTickerHelper(type, BlockEntityRegistry.VOID_PIPE_BE.get(), VoidPipeBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(final BlockState state) {
        return RenderShape.MODEL;
    }
}
