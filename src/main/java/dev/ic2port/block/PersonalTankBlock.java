package dev.ic2port.block;

import dev.ic2port.blockentity.PersonalTankBlockEntity;
import dev.ic2port.setup.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class PersonalTankBlock extends BaseEntityBlock {

    public PersonalTankBlock(final Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new PersonalTankBlockEntity(pos, state);
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
        if (!(level.getBlockEntity(pos) instanceof PersonalTankBlockEntity tank)) {
            return InteractionResult.PASS;
        }
        if (!tank.canAccess(player)) {
            return InteractionResult.FAIL;
        }
        if (player instanceof ServerPlayer serverPlayer && hand == InteractionHand.MAIN_HAND) {
            NetworkHooks.openScreen(serverPlayer, tank, pos);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean onDestroyedByPlayer(
            final BlockState state,
            final Level level,
            final BlockPos pos,
            final Player player,
            final boolean willHarvest,
            final FluidState fluid) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PersonalTankBlockEntity tank) {
            if (!tank.canAccess(player)) {
                return false;
            }
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public RenderShape getRenderShape(final BlockState state) {
        return RenderShape.MODEL;
    }
}
