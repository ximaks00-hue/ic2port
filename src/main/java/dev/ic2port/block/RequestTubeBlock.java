package dev.ic2port.block;

import dev.ic2port.blockentity.TubeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Accepts items from the tube network and inserts them into the inventory on its facing side.
 * Right-click with an item to set a filter; shift-right-click with empty hand to clear it.
 */
public class RequestTubeBlock extends DirectionalTubeBlock {

    public RequestTubeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_CYAN)
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
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof TubeBlockEntity tube)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof DyeItem) {
            return TubePaintHelper.tryPaintTube(state, level, pos, player, hand, hit);
        }
        if (held.isEmpty() && player.isShiftKeyDown()) {
            tube.clearRequestFilter();
            player.displayClientMessage(Component.translatable("message.ic2port.tube.filter_cleared"), true);
            return InteractionResult.SUCCESS;
        }
        if (!held.isEmpty()) {
            tube.setRequestFilter(held.copyWithCount(1));
            player.displayClientMessage(Component.translatable("message.ic2port.tube.filter_set", held.getHoverName()), true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
