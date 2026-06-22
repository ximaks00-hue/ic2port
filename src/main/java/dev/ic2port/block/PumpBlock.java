package dev.ic2port.block;

import dev.ic2port.blockentity.PumpBlockEntity;
import dev.ic2port.setup.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class PumpBlock extends BaseEntityBlock {

    public PumpBlock(final Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new PumpBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level, final BlockState state,
                                                                   final BlockEntityType<T> type) {
        return level.isClientSide()
                ? null
                : createTickerHelper(type, BlockEntityRegistry.PUMP_BE.get(), PumpBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(final BlockState state, final Level level, final BlockPos pos,
                                  final Player player, final InteractionHand hand, final BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PumpBlockEntity pump) {
            var tank = pump.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER, null);
            tank.ifPresent(h -> {
                var fluid = h.getFluidInTank(0);
                player.displayClientMessage(
                        Component.translatable("block.ic2port.pump.status",
                                fluid.getAmount(), PumpBlockEntity.TANK_CAPACITY_MB),
                        true);
            });
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public RenderShape getRenderShape(final BlockState state) {
        return RenderShape.MODEL;
    }
}
