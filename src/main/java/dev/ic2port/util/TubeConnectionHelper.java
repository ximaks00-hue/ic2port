package dev.ic2port.util;

import dev.ic2port.block.ITubeBlock;
import dev.ic2port.blockentity.TubeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

/**
 * Shared connection rules for item tube blocks.
 */
public final class TubeConnectionHelper {

    private TubeConnectionHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isTubeBlock(final BlockState state) {
        return state.getBlock() instanceof ITubeBlock;
    }

    public static boolean canConnectTo(
            final LevelAccessor level,
            final BlockPos tubePos,
            final Direction direction) {
        BlockPos neighborPos = tubePos.relative(direction);
        BlockState neighborState = level.getBlockState(neighborPos);

        if (isTubeBlock(neighborState)) {
            return true;
        }

        return hasItemHandler(level, neighborPos, direction.getOpposite());
    }

    public static boolean hasItemHandler(
            final LevelAccessor level,
            final BlockPos pos,
            final Direction handlerSide) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return false;
        }
        return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, handlerSide).isPresent();
    }

    public static IItemHandler getItemHandler(
            final LevelAccessor level,
            final BlockPos pos,
            final Direction handlerSide) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return null;
        }
        return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, handlerSide).orElse(null);
    }

    public static TubeBlockEntity getTube(
            final LevelAccessor level,
            final BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof TubeBlockEntity tube ? tube : null;
    }
}
