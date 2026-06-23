package dev.ic2port.util;

import dev.ic2port.blockentity.PersonalChestBlockEntity;
import dev.ic2port.item.TradeCoinItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Trade-O-Mat helpers for personal chest linking and coin payments.
 */
public final class TradeOMatHelper {

    public static final int LINK_RADIUS = 3;

    private TradeOMatHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    @Nullable
    public static PersonalChestBlockEntity findLinkedChest(final Level level, final BlockPos origin) {
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-LINK_RADIUS, -LINK_RADIUS, -LINK_RADIUS),
                origin.offset(LINK_RADIUS, LINK_RADIUS, LINK_RADIUS))) {
            if (pos.equals(origin)) {
                continue;
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof PersonalChestBlockEntity chest) {
                return chest;
            }
        }
        return null;
    }

    public static int countCoinValue(final ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof TradeCoinItem coin)) {
            return 0;
        }
        return coin.getCoinValue() * stack.getCount();
    }

    public static boolean extractFromChest(final IItemHandler chest, final ItemStack requested) {
        ItemStack remaining = requested.copy();
        for (int slot = 0; slot < chest.getSlots() && !remaining.isEmpty(); slot++) {
            ItemStack inSlot = chest.getStackInSlot(slot);
            if (inSlot.isEmpty() || !ItemStack.isSameItemSameTags(inSlot, remaining)) {
                continue;
            }
            int toExtract = Math.min(remaining.getCount(), inSlot.getCount());
            ItemStack extracted = chest.extractItem(slot, toExtract, false);
            remaining.shrink(extracted.getCount());
        }
        return remaining.isEmpty();
    }
}
