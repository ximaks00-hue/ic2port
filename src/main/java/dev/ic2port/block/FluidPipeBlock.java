package dev.ic2port.block;

import dev.ic2port.api.blocks.IFaceWrenchable;
import dev.ic2port.api.blocks.IWrenchable;
import dev.ic2port.blockentity.FluidPipeBlockEntity;
import dev.ic2port.setup.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FluidPipeBlock extends BaseEntityBlock implements IFaceWrenchable, IWrenchable {

    public FluidPipeBlock(final Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new FluidPipeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            final Level level,
            final BlockState state,
            final BlockEntityType<T> type) {
        return level.isClientSide()
                ? null
                : createTickerHelper(type, BlockEntityRegistry.FLUID_PIPE_BE.get(), FluidPipeBlockEntity::serverTick);
    }

    @Override
    public boolean onWrenchFace(final UseOnContext context, final BlockState state, final Direction face) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!(level.getBlockEntity(pos) instanceof FluidPipeBlockEntity pipe)) {
            return false;
        }
        if (level.isClientSide) {
            return true;
        }
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            pipe.toggleCover(face);
            player.displayClientMessage(
                    Component.translatable(pipe.isFaceCovered(face)
                            ? "message.ic2port.pipe.cover_added"
                            : "message.ic2port.pipe.cover_removed"),
                    true);
        } else {
            pipe.toggleConnection(face);
            if (player != null) {
                player.displayClientMessage(
                        Component.translatable(pipe.isFaceConnected(face)
                                ? "message.ic2port.pipe.face_connected"
                                : "message.ic2port.pipe.face_disconnected"),
                        true);
            }
        }
        return true;
    }

    @Override
    public RenderShape getRenderShape(final BlockState state) {
        return RenderShape.MODEL;
    }
}
