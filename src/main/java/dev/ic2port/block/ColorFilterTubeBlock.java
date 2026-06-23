package dev.ic2port.block;

import dev.ic2port.blockentity.TubeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Routes coloured items to sides configured with matching dye filters.
 */
public class ColorFilterTubeBlock extends BaseTubeBlock {

    public ColorFilterTubeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_MAGENTA)
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
        Direction side = SortingTubeBlock.resolveTubeSide(hit, pos);
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty() && player.isShiftKeyDown()) {
            tube.clearColorRoute(side);
            player.displayClientMessage(Component.translatable("message.ic2port.tube.color_route_cleared", side.name()), true);
            return InteractionResult.SUCCESS;
        }
        if (held.getItem() instanceof DyeItem dye) {
            tube.setColorRoute(side, dye.getDyeColor());
            player.displayClientMessage(Component.translatable(
                    "message.ic2port.tube.color_route_set",
                    side.name(),
                    Component.translatable("color.minecraft." + dye.getDyeColor().getName())), true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
