package dev.ic2port.block;

import dev.ic2port.blockentity.TubeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.phys.Vec3;

/**
 * Routes items toward sides whose filter matches the travelling stack.
 */
public class SortingTubeBlock extends BaseTubeBlock {

    public SortingTubeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GREEN)
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
        Direction side = resolveTubeSide(hit, pos);
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty() && player.isShiftKeyDown()) {
            tube.clearSideFilter(side);
            player.displayClientMessage(Component.translatable("message.ic2port.tube.sort_cleared", side.name()), true);
            return InteractionResult.SUCCESS;
        }
        if (!held.isEmpty()) {
            tube.setSideFilter(side, held.copyWithCount(1));
            player.displayClientMessage(Component.translatable(
                    "message.ic2port.tube.sort_set",
                    side.name(),
                    held.getHoverName()), true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    static Direction resolveTubeSide(final BlockHitResult hit, final BlockPos pos) {
        Vec3 local = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ()).subtract(0.5D, 0.5D, 0.5D);
        double absX = Math.abs(local.x);
        double absY = Math.abs(local.y);
        double absZ = Math.abs(local.z);
        if (absX >= absY && absX >= absZ) {
            return local.x > 0 ? Direction.EAST : Direction.WEST;
        }
        if (absY >= absX && absY >= absZ) {
            return local.y > 0 ? Direction.UP : Direction.DOWN;
        }
        return local.z > 0 ? Direction.SOUTH : Direction.NORTH;
    }
}
