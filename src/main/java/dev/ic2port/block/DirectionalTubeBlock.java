package dev.ic2port.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

/**
 * Tube block with a facing side toward an attached inventory.
 */
public abstract class DirectionalTubeBlock extends BaseTubeBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    protected DirectionalTubeBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected BlockState applyPlacementState(final BlockState state, final BlockPlaceContext context) {
        return state.setValue(FACING, context.getClickedFace().getOpposite());
    }

    @Override
    @Nullable
    protected Direction getInventoryFacing(final BlockState state) {
        return state.getValue(FACING);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }
}
