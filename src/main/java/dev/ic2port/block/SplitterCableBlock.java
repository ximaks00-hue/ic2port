package dev.ic2port.block;

import dev.ic2port.blockentity.SplitterCableBlockEntity;
import dev.ic2port.setup.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public class SplitterCableBlock extends CopperCableBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public SplitterCableBlock(final Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Nullable @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        return super.getStateForPlacement(context)
                .setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Nullable @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new SplitterCableBlockEntity(pos, state);
    }

    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level, final BlockState state,
                                                                   final BlockEntityType<T> type) {
        return cableTickerOrNull(
                level, type, BlockEntityRegistry.SPLITTER_CABLE_BE.get(), SplitterCableBlockEntity::serverTick);
    }
}
