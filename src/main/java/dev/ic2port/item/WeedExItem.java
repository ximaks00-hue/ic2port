package dev.ic2port.item;

import dev.ic2port.blockentity.CropSticksBlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Protects crop sticks from weeds (IC2 weed-ex).
 */
public class WeedExItem extends Item {

    public static final int MAX_STORAGE = 150;
    public static final int APPLY_AMOUNT = 64;

    public WeedExItem(final Properties properties) {
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
        if (crop.getCrop() == null) {
            return InteractionResult.PASS;
        }
        int storage = crop.getWeedExStorage();
        if (storage > 0) {
            crop.setGainStat(Math.max(1, crop.getGainStat() - 1));
        }
        if (storage >= MAX_STORAGE) {
            return InteractionResult.PASS;
        }
        crop.setWeedExStorage(Math.min(MAX_STORAGE, storage + APPLY_AMOUNT));
        crop.setChanged();
        if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        level.levelEvent(1505, context.getClickedPos(), 0);
        return InteractionResult.CONSUME;
    }
}
