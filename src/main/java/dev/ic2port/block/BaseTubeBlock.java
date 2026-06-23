package dev.ic2port.block;

import dev.ic2port.blockentity.TubeBlockEntity;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.util.TubeConnectionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Shared multipart tube geometry and neighbor connection logic.
 */
public abstract class BaseTubeBlock extends BaseEntityBlock implements ITubeBlock {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    private static final Direction[] DIRECTIONS = Direction.values();

    private static final VoxelShape CORE_SHAPE = box(6.0D, 6.0D, 6.0D, 10.0D, 10.0D, 10.0D);
    private static final VoxelShape NORTH_SHAPE = box(6.0D, 6.0D, 0.0D, 10.0D, 10.0D, 6.0D);
    private static final VoxelShape SOUTH_SHAPE = box(6.0D, 6.0D, 10.0D, 10.0D, 10.0D, 16.0D);
    private static final VoxelShape EAST_SHAPE = box(10.0D, 6.0D, 6.0D, 16.0D, 10.0D, 10.0D);
    private static final VoxelShape WEST_SHAPE = box(0.0D, 6.0D, 6.0D, 6.0D, 10.0D, 10.0D);
    private static final VoxelShape UP_SHAPE = box(6.0D, 10.0D, 6.0D, 10.0D, 16.0D, 10.0D);
    private static final VoxelShape DOWN_SHAPE = box(6.0D, 0.0D, 6.0D, 10.0D, 6.0D, 10.0D);

    private static final VoxelShape[] SHAPES = new VoxelShape[64];

    static {
        for (int mask = 0; mask < SHAPES.length; mask++) {
            VoxelShape shape = CORE_SHAPE;
            if ((mask & 1) != 0) {
                shape = Shapes.or(shape, NORTH_SHAPE);
            }
            if ((mask & 2) != 0) {
                shape = Shapes.or(shape, SOUTH_SHAPE);
            }
            if ((mask & 4) != 0) {
                shape = Shapes.or(shape, EAST_SHAPE);
            }
            if ((mask & 8) != 0) {
                shape = Shapes.or(shape, WEST_SHAPE);
            }
            if ((mask & 16) != 0) {
                shape = Shapes.or(shape, UP_SHAPE);
            }
            if ((mask & 32) != 0) {
                shape = Shapes.or(shape, DOWN_SHAPE);
            }
            SHAPES[mask] = shape;
        }
    }

    protected BaseTubeBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new TubeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            final Level level,
            final BlockState state,
            final BlockEntityType<T> type) {
        return level.isClientSide()
                ? null
                : createTickerHelper(type, BlockEntityRegistry.TUBE_BE.get(), TubeBlockEntity::serverTick);
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        return updateConnections(applyPlacementState(this.defaultBlockState(), context), context.getLevel(), context.getClickedPos());
    }

    protected BlockState applyPlacementState(final BlockState state, final BlockPlaceContext context) {
        return state;
    }

    @Nullable
    protected Direction getInventoryFacing(final BlockState state) {
        return null;
    }

    @Override
    public BlockState updateShape(
            final BlockState state,
            final Direction direction,
            final BlockState neighborState,
            final LevelAccessor level,
            final BlockPos currentPos,
            final BlockPos neighborPos) {
        return state.setValue(propertyFor(direction), canConnectTo(level, currentPos, state, direction));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Override
    public VoxelShape getShape(
            final BlockState state,
            final BlockGetter level,
            final BlockPos pos,
            final CollisionContext context) {
        return SHAPES[shapeIndex(state)];
    }

    @Override
    public VoxelShape getCollisionShape(
            final BlockState state,
            final BlockGetter level,
            final BlockPos pos,
            final CollisionContext context) {
        return SHAPES[shapeIndex(state)];
    }

    @Override
    public RenderShape getRenderShape(final BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(
            final BlockState state,
            final Level level,
            final BlockPos pos,
            final Player player,
            final InteractionHand hand,
            final BlockHitResult hit) {
        return TubePaintHelper.tryPaintTube(state, level, pos, player, hand, hit);
    }

    @Override
    public boolean hasAnalogOutputSignal(final BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(
            final BlockState state,
            final Level level,
            final BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TubeBlockEntity tube) {
            return tube.getComparatorOutput();
        }
        return 0;
    }

    protected BlockState updateConnections(final BlockState state, final LevelAccessor level, final BlockPos pos) {
        BlockState result = state;
        for (Direction direction : DIRECTIONS) {
            result = result.setValue(propertyFor(direction), canConnectTo(level, pos, result, direction));
        }
        return result;
    }

    protected boolean canConnectTo(
            final LevelAccessor level,
            final BlockPos tubePos,
            final BlockState state,
            final Direction direction) {
        Direction inventoryFacing = getInventoryFacing(state);
        if (inventoryFacing != null && direction == inventoryFacing) {
            return TubeConnectionHelper.canConnectTo(level, tubePos, direction);
        }
        return TubeConnectionHelper.canConnectTo(level, tubePos, direction);
    }

    protected static BooleanProperty propertyFor(final Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    public static boolean isConnected(final BlockState state, final Direction direction) {
        return state.getValue(propertyFor(direction));
    }

    protected static int shapeIndex(final BlockState state) {
        int index = 0;
        if (state.getValue(NORTH)) {
            index |= 1;
        }
        if (state.getValue(SOUTH)) {
            index |= 2;
        }
        if (state.getValue(EAST)) {
            index |= 4;
        }
        if (state.getValue(WEST)) {
            index |= 8;
        }
        if (state.getValue(UP)) {
            index |= 16;
        }
        if (state.getValue(DOWN)) {
            index |= 32;
        }
        return index;
    }
}
