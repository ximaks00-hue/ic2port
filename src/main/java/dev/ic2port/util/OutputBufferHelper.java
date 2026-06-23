package dev.ic2port.util;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

/**
 * Merges stacks into output buffers whose {@code isItemValid} rejects hopper insert.
 */
public final class OutputBufferHelper {

    private OutputBufferHelper() {
    }

    public static ItemStack insert(final ItemStackHandler handler, final ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            ItemStack existing = handler.getStackInSlot(slot);
            if (existing.isEmpty() || !ItemStack.isSameItemSameTags(existing, remaining)) {
                continue;
            }
            int limit = Math.min(handler.getSlotLimit(slot), existing.getMaxStackSize());
            int move = Math.min(remaining.getCount(), limit - existing.getCount());
            if (move <= 0) {
                continue;
            }
            existing.grow(move);
            handler.setStackInSlot(slot, existing);
            remaining.shrink(move);
        }
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            if (!handler.getStackInSlot(slot).isEmpty()) {
                continue;
            }
            int place = Math.min(
                    remaining.getCount(),
                    Math.min(handler.getSlotLimit(slot), remaining.getMaxStackSize()));
            ItemStack placed = remaining.copy();
            placed.setCount(place);
            handler.setStackInSlot(slot, placed);
            remaining.shrink(place);
        }
        return remaining;
    }

    public static boolean canFitAll(final ItemStackHandler handler, final List<ItemStack> stacks) {
        if (stacks.isEmpty()) {
            return true;
        }
        ItemStackHandler simulation = copyHandler(handler);
        for (ItemStack stack : stacks) {
            if (!insert(simulation, stack.copy()).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static ItemStack insertRange(
            final ItemStackHandler handler,
            final int startSlot,
            final int slotCount,
            final ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int slot = startSlot; slot < startSlot + slotCount && !remaining.isEmpty(); slot++) {
            ItemStack inSlot = handler.getStackInSlot(slot);
            if (inSlot.isEmpty()) {
                handler.setStackInSlot(slot, remaining);
                return ItemStack.EMPTY;
            }
            if (ItemStack.isSameItemSameTags(inSlot, remaining)) {
                int move = Math.min(remaining.getCount(), inSlot.getMaxStackSize() - inSlot.getCount());
                if (move > 0) {
                    inSlot.grow(move);
                    handler.setStackInSlot(slot, inSlot);
                    remaining.shrink(move);
                }
            }
        }
        return remaining;
    }

    public static boolean canFitAll(
            final ItemStackHandler handler,
            final int startSlot,
            final int slotCount,
            final List<ItemStack> stacks) {
        if (stacks.isEmpty()) {
            return true;
        }
        ItemStackHandler simulation = new ItemStackHandler(handler.getSlots()) {
            @Override
            public boolean isItemValid(final int slot, final ItemStack stack) {
                return false;
            }
        };
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            simulation.setStackInSlot(slot, handler.getStackInSlot(slot).copy());
        }
        for (ItemStack stack : stacks) {
            if (!insertRange(simulation, startSlot, slotCount, stack.copy()).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static ItemStackHandler copyHandler(final ItemStackHandler handler) {
        ItemStackHandler simulation = new ItemStackHandler(handler.getSlots()) {
            @Override
            public boolean isItemValid(final int slot, final ItemStack stack) {
                return false;
            }
        };
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            simulation.setStackInSlot(slot, handler.getStackInSlot(slot).copy());
        }
        return simulation;
    }
}
