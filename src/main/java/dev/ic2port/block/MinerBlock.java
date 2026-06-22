package dev.ic2port.block;

import dev.ic2port.blockentity.MinerBlockEntity;
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

public class MinerBlock extends BaseEntityBlock {

    public MinerBlock(final Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new MinerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level, final BlockState state,
                                                                   final BlockEntityType<T> type) {
        return level.isClientSide()
                ? null
                : createTickerHelper(type, BlockEntityRegistry.MINER_BE.get(), MinerBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(final BlockState state, final Level level, final BlockPos pos,
                                  final Player player, final InteractionHand hand, final BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof MinerBlockEntity miner) {
            double energy = miner.getStoredEnergy();
            player.displayClientMessage(
                    Component.translatable("block.ic2port.miner.status",
                            (int) energy, (int) MinerBlockEntity.ENERGY_CAPACITY),
                    true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public RenderShape getRenderShape(final BlockState state) {
        return RenderShape.MODEL;
    }
}
