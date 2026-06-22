package dev.ic2port.util;

import dev.ic2port.setup.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Shared wet construction foam placement for manual and electric sprayers.
 */
public final class FoamSprayHelper {

    public static final int MAX_RANGE = 8;

    private FoamSprayHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static int sprayFromLook(
            final Level level,
            final Player player,
            final ItemStack sprayer,
            final InteractionHand hand,
            final int maxBlocks,
            final FoamCost cost) {
        Vec3 look = player.getLookAngle();
        BlockPos start = player.blockPosition();
        int placed = 0;
        for (int step = 1; step <= MAX_RANGE && placed < maxBlocks; step++) {
            BlockPos target = BlockPos.containing(
                    start.getX() + 0.5D + look.x * step,
                    start.getY() + 0.5D + look.y * step,
                    start.getZ() + 0.5D + look.z * step);
            if (tryPlaceFoam(level, player, sprayer, hand, target, cost)) {
                placed++;
            } else if (level.getBlockState(target).canOcclude()) {
                break;
            }
        }
        if (placed > 0) {
            level.playSound(null, player.blockPosition(), SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.7F, 0.9F);
        }
        return placed;
    }

    public static int sprayFromFace(
            final Level level,
            final Player player,
            final ItemStack sprayer,
            final InteractionHand hand,
            final BlockPos start,
            final int maxBlocks,
            final FoamCost cost) {
        Vec3 look = player.getLookAngle();
        int placed = 0;
        for (int step = 0; step < MAX_RANGE && placed < maxBlocks; step++) {
            BlockPos target = start.offset(
                    Mth.floor(look.x * step),
                    Mth.floor(look.y * step),
                    Mth.floor(look.z * step));
            if (tryPlaceFoam(level, player, sprayer, hand, target, cost)) {
                placed++;
            }
        }
        if (placed > 0) {
            level.playSound(null, start, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.7F, 0.9F);
        }
        return placed;
    }

    private static boolean tryPlaceFoam(
            final Level level,
            final Player player,
            final ItemStack sprayer,
            final InteractionHand hand,
            final BlockPos pos,
            final FoamCost cost) {
        BlockState state = level.getBlockState(pos);
        if (!state.canBeReplaced() && !state.isAir()) {
            return false;
        }
        if (!level.getBlockState(pos.below()).isSolidRender(level, pos.below()) && !state.isAir()) {
            return false;
        }
        if (!cost.pay(player)) {
            return false;
        }
        level.setBlock(pos, BlockRegistry.WET_CONSTRUCTION_FOAM.get().defaultBlockState(), 3);
        if (!player.getAbilities().instabuild && sprayer.isDamageableItem()) {
            sprayer.hurtAndBreak(1, player, user -> user.broadcastBreakEvent(hand));
        }
        return true;
    }

    /**
     * Consumes one foam unit per placed block.
     */
    @FunctionalInterface
    public interface FoamCost {
        boolean pay(Player player);
    }
}
