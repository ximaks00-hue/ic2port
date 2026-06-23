package dev.ic2port.util;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure item-routing helpers for tube logistics.
 */
public final class TubeTransferHelper {

    private TubeTransferHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Directions to attempt when pushing an item out of a tube, excluding the entry side.
     */
    public static List<Direction> pushDirections(final Direction entryDirection, final int startIndex) {
        List<Direction> directions = new ArrayList<>(5);
        int normalizedStart = Math.floorMod(startIndex, 6);
        for (int offset = 0; offset < 6; offset++) {
            Direction direction = Direction.from3DDataValue((normalizedStart + offset) % 6);
            if (entryDirection != null && direction == entryDirection) {
                continue;
            }
            directions.add(direction);
        }
        return directions;
    }

    public static boolean matchesFilter(final ItemStack filter, final ItemStack stack) {
        if (filter.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameTags(stack, filter);
    }

    public static ItemStack extractOneItem(final IItemHandler handler) {
        return extractMatchingItem(handler, ItemStack.EMPTY);
    }

    public static ItemStack extractMatchingItem(final IItemHandler handler, final ItemStack filter) {
        if (handler == null) {
            return ItemStack.EMPTY;
        }
        if (!filter.isEmpty()) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack simulated = handler.extractItem(slot, 1, true);
                if (!simulated.isEmpty() && matchesFilter(filter, simulated)) {
                    return handler.extractItem(slot, 1, false);
                }
            }
            return ItemStack.EMPTY;
        }
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack simulated = handler.extractItem(slot, 1, true);
            if (!simulated.isEmpty()) {
                return handler.extractItem(slot, 1, false);
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack insertIntoHandler(final IItemHandler handler, final ItemStack stack) {
        if (handler == null || stack.isEmpty()) {
            return stack;
        }
        return ItemHandlerHelper.insertItem(handler, stack, false);
    }

    public static boolean handlerContainsMatchingItem(final IItemHandler handler, final ItemStack stack) {
        if (handler == null || stack.isEmpty()) {
            return false;
        }
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack contained = handler.getStackInSlot(slot);
            if (!contained.isEmpty() && ItemStack.isSameItemSameTags(contained, stack)) {
                return true;
            }
        }
        return false;
    }

    public static int computeFillRatio(final IItemHandler handler) {
        if (handler == null || handler.getSlots() == 0) {
            return 0;
        }
        int filled = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (!handler.getStackInSlot(slot).isEmpty()) {
                filled++;
            }
        }
        return (int) Math.round((filled / (double) handler.getSlots()) * 15.0D);
    }
}
