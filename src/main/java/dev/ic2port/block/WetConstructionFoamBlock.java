package dev.ic2port.block;

import dev.ic2port.util.ConstructionFoamHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Wet construction foam — dries in light or instantly with sand / concrete powder.
 */
public class WetConstructionFoamBlock extends Block {

    private static final int DRY_TICKS_BRIGHT = 80;
    private static final int DRY_TICKS_DIM = 400;

    public WetConstructionFoamBlock(final Properties properties) {
        super(properties);
    }

    @Override
    public boolean isRandomlyTicking(final BlockState state) {
        return true;
    }

    @Override
    public void randomTick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        int light = level.getMaxLocalRawBrightness(pos);
        if (light < 4) {
            return;
        }
        int threshold = light >= 9 ? DRY_TICKS_BRIGHT : DRY_TICKS_DIM;
        if (random.nextInt(threshold) == 0) {
            ConstructionFoamHelper.dryBlock(level, pos, null);
        }
    }

    @Override
    public InteractionResult use(
            final BlockState state,
            final Level level,
            final BlockPos pos,
            final Player player,
            final InteractionHand hand,
            final BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!ConstructionFoamHelper.isDryingAgent(stack)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (ConstructionFoamHelper.dryBlock(level, pos, ConstructionFoamHelper.getDyeFromAgent(stack))) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.playSound(null, pos, SoundEvents.SAND_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }
}
