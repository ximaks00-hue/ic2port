package dev.ic2port.block;

import dev.ic2port.blockentity.TubeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Extracts only matching items; supports filter and tube configurator upgrades.
 */
public class FilteredExtractionTubeBlock extends DirectionalTubeBlock {

    public FilteredExtractionTubeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_ORANGE)
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
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof TubeBlockEntity tube)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty() && player.isShiftKeyDown()) {
            tube.clearExtractionFilter();
            player.displayClientMessage(Component.translatable("message.ic2port.tube.extraction_filter_cleared"), true);
            return InteractionResult.SUCCESS;
        }
        if (!held.isEmpty()) {
            tube.setExtractionFilter(held.copyWithCount(1));
            player.displayClientMessage(Component.translatable(
                    "message.ic2port.tube.extraction_filter_set",
                    held.getHoverName()), true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
