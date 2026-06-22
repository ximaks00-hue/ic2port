package dev.ic2port.block;

import dev.ic2port.item.AdvancedTreeTapItem;
import dev.ic2port.item.ElectricTreeTapItem;
import dev.ic2port.item.TreeTapItem;
import dev.ic2port.util.RubberResinExtractor;
import dev.ic2port.util.RubberTreeHelper;
import dev.ic2port.setup.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Rubber tree log — may carry a refillable resin spot for the {@link dev.ic2port.item.TreeTapItem}.
 */
public class RubberWoodBlock extends RotatedPillarBlock {

    public static final BooleanProperty RESIN = BooleanProperty.create("resin");
    public static final BooleanProperty DEPLETED = BooleanProperty.create("depleted");

    private static final int RESIN_REGROW_CHANCE = 64;

    public RubberWoodBlock(final Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(RESIN, false).setValue(DEPLETED, false));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(RESIN, DEPLETED);
    }

    @Override
    public void randomTick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        if (state.getValue(DEPLETED) || state.getValue(RESIN)) {
            return;
        }
        if (state.getValue(AXIS) != Direction.Axis.Y) {
            return;
        }
        if (!hasRubberLeavesNearby(level, pos)) {
            return;
        }
        if (random.nextInt(RESIN_REGROW_CHANCE) != 0) {
            return;
        }
        level.setBlock(pos, state.setValue(RESIN, true), 3);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(
            final BlockState state,
            final Level level,
            final BlockPos pos,
            final Player player,
            final InteractionHand hand,
            final BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof TreeTapItem) {
            return RubberResinExtractor.tryExtract(
                    state,
                    level,
                    pos,
                    player,
                    hand,
                    held,
                    RubberResinExtractor.durabilityCost(),
                    RubberResinExtractor.MANUAL_OVERTAP_CHANCE);
        }
        if (held.getItem() instanceof ElectricTreeTapItem electric) {
            return RubberResinExtractor.tryExtract(
                    state,
                    level,
                    pos,
                    player,
                    hand,
                    held,
                    electric::payTapCostForBlock,
                    RubberResinExtractor.ELECTRIC_OVERTAP_CHANCE);
        }
        if (held.getItem() instanceof AdvancedTreeTapItem) {
            return RubberTreeHelper.stripTree(level, pos, player, hand, held, AdvancedTreeTapItem.DURABILITY_PER_TREE);
        }
        return InteractionResult.PASS;
    }

    static boolean hasRubberLeavesNearby(final BlockGetter level, final BlockPos origin) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -1; dy <= 5; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    cursor.setWithOffset(origin, dx, dy, dz);
                    if (level.getBlockState(cursor).is(BlockRegistry.RUBBER_LEAVES.get())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void playTapSound(final Level level, final BlockPos pos) {
        level.playSound(
                null,
                pos,
                SoundEvents.SLIME_BLOCK_PLACE,
                SoundSource.BLOCKS,
                0.7F,
                0.9F + level.random.nextFloat() * 0.2F);
    }
}
