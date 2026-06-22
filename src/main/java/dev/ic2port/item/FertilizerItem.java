package dev.ic2port.item;

import dev.ic2port.blockentity.CropSticksBlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * IC2-style fertilizer — stronger growth boost than bone meal on crop sticks.
 */
public class FertilizerItem extends Item {

    public static final int GROWTH_BOOST = 64;

    public FertilizerItem(final Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        Level level = context.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());
        if (!(blockEntity instanceof CropSticksBlockEntity crop)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!crop.tryFertilize(GROWTH_BOOST)) {
            return InteractionResult.PASS;
        }
        if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        level.levelEvent(1505, context.getClickedPos(), 0);
        return InteractionResult.CONSUME;
    }
}
