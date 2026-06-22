package dev.ic2port.item;

import dev.ic2port.util.PainterHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * IC2-style painter — efficient dye application to foam and vanilla blocks.
 */
public class PainterItem extends Item {

    public static final int DYE_EFFICIENCY = 8;

    public PainterItem(final Properties properties) {
        super(properties.durability(512));
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        Level level = context.getLevel();
        BlockPos origin = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        DyeColor color = findSelectedDye(player);
        if (color == null) {
            return InteractionResult.PASS;
        }
        if (!PainterHelper.canPaint(level.getBlockState(origin))) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        int radius = player.isShiftKeyDown() ? 0 : 1;
        int painted = paintArea(level, origin, radius, color);
        if (painted <= 0) {
            return InteractionResult.PASS;
        }

        consumeDye(player, painted);
        context.getItemInHand().hurtAndBreak(1, player, user -> user.broadcastBreakEvent(context.getHand()));
        level.playSound(null, origin, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.7F, 1.2F);
        return InteractionResult.CONSUME;
    }

    private static int paintArea(
            final Level level,
            final BlockPos origin,
            final int radius,
            final DyeColor color) {
        int painted = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    cursor.setWithOffset(origin, x, y, z);
                    if (PainterHelper.paintBlock(level, cursor, color)) {
                        painted++;
                    }
                }
            }
        }
        return painted;
    }

    private static DyeColor findSelectedDye(final Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof DyeItem dye) {
                return dye.getDyeColor();
            }
        }
        return null;
    }

    private static void consumeDye(final Player player, final int blocksPainted) {
        int dyesNeeded = Math.max(1, (blocksPainted + DYE_EFFICIENCY - 1) / DYE_EFFICIENCY);
        for (int slot = 0; slot < player.getInventory().getContainerSize() && dyesNeeded > 0; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof DyeItem) {
                int used = Math.min(stack.getCount(), dyesNeeded);
                stack.shrink(used);
                dyesNeeded -= used;
            }
        }
    }
}
