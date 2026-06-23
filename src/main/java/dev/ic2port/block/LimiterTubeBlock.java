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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Only allows items with matching dye colors to pass (uncolored items pass when no colors configured).
 */
public class LimiterTubeBlock extends BaseTubeBlock {

    public LimiterTubeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
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
        if (held.isEmpty() && player.isShiftKeyDown()) {
            tube.clearAllowedColors();
            player.displayClientMessage(Component.translatable("message.ic2port.tube.limiter_cleared"), true);
            return InteractionResult.SUCCESS;
        }
        if (held.getItem() instanceof DyeItem dye) {
            if (player.isShiftKeyDown()) {
                tube.toggleAllowedColor(dye.getDyeColor());
                player.displayClientMessage(Component.translatable(
                        "message.ic2port.tube.limiter_color",
                        Component.translatable("color.minecraft." + dye.getDyeColor().getName())), true);
            } else {
                return TubePaintHelper.tryPaintTube(state, level, pos, player, hand, hit);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
