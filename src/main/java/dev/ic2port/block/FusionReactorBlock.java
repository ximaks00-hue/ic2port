package dev.ic2port.block;

import dev.ic2port.blockentity.FusionReactorBlockEntity;
import dev.ic2port.setup.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class FusionReactorBlock extends BaseEntityBlock {

    public FusionReactorBlock(final Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new FusionReactorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            final Level level,
            final BlockState state,
            final BlockEntityType<T> type) {
        return level.isClientSide()
                ? null
                : createTickerHelper(
                        type,
                        BlockEntityRegistry.FUSION_REACTOR_BE.get(),
                        FusionReactorBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(
            final BlockState state,
            final Level level,
            final BlockPos pos,
            final Player player,
            final InteractionHand hand,
            final BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof FusionReactorBlockEntity reactor)) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown() && player.getItemInHand(hand).is(Items.COMPARATOR)) {
            reactor.toggleComparatorHeatMode();
            player.displayClientMessage(
                    Component.translatable(
                            reactor.isComparatorHeatMode()
                                    ? "message.ic2port.fusion_reactor.comparator_mode_heat"
                                    : "message.ic2port.fusion_reactor.comparator_mode_lava"),
                    true);
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty() && hand == InteractionHand.MAIN_HAND) {
            reactor.toggleAutoExportLava();
            player.displayClientMessage(
                    Component.translatable(
                            reactor.isAutoExportLava()
                                    ? "message.ic2port.fusion_reactor.auto_export_on"
                                    : "message.ic2port.fusion_reactor.auto_export_off"),
                    true);
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (player instanceof ServerPlayer serverPlayer && hand == InteractionHand.MAIN_HAND) {
            NetworkHooks.openScreen(serverPlayer, reactor, pos);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public RenderShape getRenderShape(final BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean hasAnalogOutputSignal(final BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(final BlockState state, final Level level, final BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof FusionReactorBlockEntity reactor) {
            return reactor.getComparatorOutput();
        }
        return 0;
    }
}
