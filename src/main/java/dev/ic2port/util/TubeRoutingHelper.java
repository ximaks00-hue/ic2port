package dev.ic2port.util;

import dev.ic2port.tube.TransportedItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

/**
 * Color, limiter, and sorting helpers for tube logistics.
 */
public final class TubeRoutingHelper {

    private TubeRoutingHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean passesLimiter(
            final EnumSet<DyeColor> allowedColors,
            final TransportedItem item) {
        return passesColor(allowedColors, item.getColor());
    }

    public static boolean passesColor(
            final EnumSet<DyeColor> allowedColors,
            final DyeColor itemColor) {
        if (allowedColors.isEmpty()) {
            return true;
        }
        return itemColor != null && allowedColors.contains(itemColor);
    }

    public static void applyTubePaint(
            final TransportedItem item,
            final DyeColor paintColor) {
        if (paintColor != null) {
            item.setColor(paintColor);
        }
    }

    public static List<Direction> sortingDirections(
            final Direction entryDirection,
            final int startIndex,
            final ItemStack stack,
            final Map<Direction, ItemStack> sideFilters) {
        List<Direction> matching = new ArrayList<>();
        List<Direction> neutral = new ArrayList<>();
        for (Direction direction : TubeTransferHelper.pushDirections(entryDirection, startIndex)) {
            ItemStack filter = sideFilters.getOrDefault(direction, ItemStack.EMPTY);
            if (filter.isEmpty()) {
                neutral.add(direction);
            } else if (TubeTransferHelper.matchesFilter(filter, stack)) {
                matching.add(direction);
            }
        }
        if (!matching.isEmpty()) {
            return matching;
        }
        return neutral;
    }

    public static List<Direction> pushDirections(
            final Direction entryDirection,
            final int startIndex) {
        return TubeTransferHelper.pushDirections(entryDirection, startIndex);
    }

    public static List<Direction> filterDirections(
            final Direction entryDirection,
            final int startIndex,
            final ItemStack stack,
            final net.minecraftforge.items.ItemStackHandler filterHandler,
            @Nullable final Direction defaultDirection) {
        for (int slot = 0; slot < filterHandler.getSlots(); slot++) {
            ItemStack filter = filterHandler.getStackInSlot(slot);
            if (!filter.isEmpty() && TubeTransferHelper.matchesFilter(filter, stack)) {
                return List.of(Direction.from3DDataValue(slot % 6));
            }
        }
        if (defaultDirection != null) {
            return List.of(defaultDirection);
        }
        return pushDirections(entryDirection, startIndex);
    }

    public static List<Direction> colorFilterDirections(
            final Direction entryDirection,
            final int startIndex,
            @Nullable final DyeColor itemColor,
            final Map<Direction, DyeColor> colorRoutes) {
        if (itemColor != null) {
            for (Map.Entry<Direction, DyeColor> entry : colorRoutes.entrySet()) {
                if (entry.getValue() == itemColor) {
                    return List.of(entry.getKey());
                }
            }
        }
        return pushDirections(entryDirection, startIndex);
    }

    public static List<Direction> roundRobinDirection(
            final Direction entryDirection,
            final int startIndex) {
        List<Direction> directions = pushDirections(entryDirection, startIndex);
        if (directions.isEmpty()) {
            return directions;
        }
        return List.of(directions.get(0));
    }

    public static List<Direction> insertionDirections(
            final Direction entryDirection,
            final int startIndex,
            final net.minecraft.world.level.Level level,
            final net.minecraft.core.BlockPos tubePos,
            final net.minecraft.world.level.block.state.BlockState state) {
        List<Direction> inventories = new ArrayList<>();
        List<Direction> tubes = new ArrayList<>();
        for (Direction direction : pushDirections(entryDirection, startIndex)) {
            if (!dev.ic2port.block.BaseTubeBlock.isConnected(state, direction)) {
                continue;
            }
            BlockPos neighborPos = tubePos.relative(direction);
            if (TubeConnectionHelper.getTube(level, neighborPos) != null) {
                tubes.add(direction);
            } else if (TubeConnectionHelper.hasItemHandler(level, neighborPos, direction.getOpposite())) {
                inventories.add(direction);
            } else {
                tubes.add(direction);
            }
        }
        List<Direction> ordered = new ArrayList<>(inventories);
        ordered.addAll(tubes);
        return ordered;
    }
}
