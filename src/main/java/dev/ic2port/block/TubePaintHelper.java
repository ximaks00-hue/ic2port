package dev.ic2port.block;

import dev.ic2port.blockentity.TubeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Shared dye interactions for tube blocks.
 */
public final class TubePaintHelper {

    private TubePaintHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static InteractionResult tryPaintTube(
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
        if (held.isEmpty() && player.isShiftKeyDown()) {
            tube.setPaintColor(null);
            player.displayClientMessage(Component.translatable("message.ic2port.tube.paint_cleared"), true);
            return InteractionResult.SUCCESS;
        }
        if (held.getItem() instanceof DyeItem dye) {
            tube.setPaintColor(dye.getDyeColor());
            player.displayClientMessage(Component.translatable(
                    "message.ic2port.tube.painted",
                    Component.translatable("color.minecraft." + dye.getDyeColor().getName())), true);
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
