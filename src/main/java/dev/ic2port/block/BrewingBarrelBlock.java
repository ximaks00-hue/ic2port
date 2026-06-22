package dev.ic2port.block;

import dev.ic2port.blockentity.BrewingBarrelBlockEntity;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/**
 * IC2-style brewing barrel — place on scaffold/planks; open with a tree tap.
 */
public class BrewingBarrelBlock extends BaseEntityBlock {

    public BrewingBarrelBlock(final Properties properties) {
        super(properties);
    }

    @Override
    public boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos pos) {
        return hasSupport(level, pos.below());
    }

    private static boolean hasSupport(final LevelReader level, final BlockPos below) {
        BlockState support = level.getBlockState(below);
        return support.getBlock() instanceof ScaffoldingBlock
                || support.is(BlockTags.PLANKS)
                || support.is(BlockTags.WOODEN_SLABS);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        return hasSupport(context.getLevel(), context.getClickedPos().below())
                ? defaultBlockState()
                : null;
    }

    @Override
    public void onPlace(final BlockState state, final Level level, final BlockPos pos, final BlockState oldState, final boolean isMoving) {
        if (!hasSupport(level, pos.below())) {
            level.destroyBlock(pos, true);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new BrewingBarrelBlockEntity(pos, state);
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
                        BlockEntityRegistry.BREWING_BARREL_BE.get(),
                        BrewingBarrelBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(
            final BlockState state,
            final Level level,
            final BlockPos pos,
            final Player player,
            final InteractionHand hand,
            final BlockHitResult hit) {
        if (!hasTreeTap(player)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof BrewingBarrelBlockEntity barrel)) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, barrel, pos);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    private static boolean hasTreeTap(final Player player) {
        return isTreeTap(player.getMainHandItem()) || isTreeTap(player.getOffhandItem());
    }

    private static boolean isTreeTap(final ItemStack stack) {
        return stack.is(ItemRegistry.TREE_TAP.get())
                || stack.is(ItemRegistry.ELECTRIC_TREE_TAP.get())
                || stack.is(ItemRegistry.ADVANCED_TREE_TAP.get());
    }

    @Override
    public RenderShape getRenderShape(final BlockState state) {
        return RenderShape.MODEL;
    }
}
