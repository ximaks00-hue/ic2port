package dev.ic2port.block;

import dev.ic2port.blockentity.CropSticksBlockEntity;
import dev.ic2port.setup.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Crop sticks placed on farmland — hosts IC2-style breeding crops.
 */
public class CropSticksBlock extends BaseEntityBlock {

    public static final int MAX_STAGE = 4;
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, MAX_STAGE);

    private static final VoxelShape[] SHAPES = new VoxelShape[] {
            Block.box(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D),
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 8.0D, 12.0D),
            Block.box(3.0D, 0.0D, 3.0D, 13.0D, 12.0D, 13.0D),
            Block.box(2.0D, 0.0D, 2.0D, 14.0D, 14.0D, 14.0D),
            Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D)
    };

    public CropSticksBlock(final Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(STAGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new CropSticksBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(final BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
        return SHAPES[Math.min(MAX_STAGE, state.getValue(STAGE))];
    }

    @Override
    public boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(Blocks.FARMLAND) || below.is(Blocks.DIRT) || below.is(Blocks.GRASS_BLOCK);
    }

    @Override
    public boolean isRandomlyTicking(final BlockState state) {
        return true;
    }

    @Override
    public void randomTick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof CropSticksBlockEntity crop) {
            crop.onRandomTick(random);
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
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof CropSticksBlockEntity crop)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.BONE_MEAL) && crop.tryFertilize()) {
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            level.levelEvent(1505, pos, 0);
            return InteractionResult.CONSUME;
        }
        if (!held.isEmpty() && crop.tryPlant(player, held)) {
            return InteractionResult.CONSUME;
        }
        if (crop.tryHarvest(player)) {
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void attack(final BlockState state, final Level level, final BlockPos pos, final Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof CropSticksBlockEntity crop) {
            crop.tryPick(player);
        }
        super.attack(state, level, pos, player);
    }

    @Override
    public void onRemove(final BlockState state, final Level level, final BlockPos pos, final BlockState newState, final boolean isMoving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CropSticksBlockEntity crop) {
            crop.dropContents();
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
