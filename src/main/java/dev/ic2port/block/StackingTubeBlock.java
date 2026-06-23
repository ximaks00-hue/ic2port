package dev.ic2port.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Accumulates items until a stack threshold is reached, then outputs on the facing side.
 */
public class StackingTubeBlock extends DirectionalTubeBlock {

    public StackingTubeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_YELLOW)
                .strength(0.5F)
                .noOcclusion()
                .sound(SoundType.METAL));
    }

    @Override
    public InteractionResult use(
            final BlockState state,
            final Level level,
            final BlockPos pos,
            final Player player,
            final InteractionHand hand,
            final BlockHitResult hit) {
        InteractionResult paint = TubePaintHelper.tryPaintTube(state, level, pos, player, hand, hit);
        if (paint != InteractionResult.PASS) {
            return paint;
        }
        if (player.isShiftKeyDown()) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof dev.ic2port.blockentity.TubeBlockEntity tube) {
                tube.cycleStackingThreshold();
            }
            return InteractionResult.CONSUME;
        }
        if (!player.getItemInHand(hand).isEmpty()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        MenuProvider provider = state.getMenuProvider(level, pos);
        if (provider != null) {
            player.openMenu(provider);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(final BlockState state, final Level level, final BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof MenuProvider provider ? provider : null;
    }
}
