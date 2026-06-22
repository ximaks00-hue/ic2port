package dev.ic2port.item;

import dev.ic2port.blockentity.CropSticksBlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Adds humidity storage to crop sticks (IC2 hydration cell).
 */
public class HydrationCellItem extends Item {

    public static final int MAX_STORAGE = 150;
    public static final int APPLY_AMOUNT = 32;

    public HydrationCellItem(final Properties properties) {
        super(properties.durability(64));
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
        int storage = crop.getHydrationStorage();
        if (storage >= MAX_STORAGE) {
            return InteractionResult.PASS;
        }
        int added = Math.min(APPLY_AMOUNT, MAX_STORAGE - storage);
        crop.setHydrationStorage(storage + added);
        crop.setChanged();
        if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().hurtAndBreak(1, context.getPlayer(), user -> user.broadcastBreakEvent(context.getHand()));
        }
        level.levelEvent(1505, context.getClickedPos(), 0);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean isBarVisible(final net.minecraft.world.item.ItemStack stack) {
        return stack.isDamaged();
    }
}
