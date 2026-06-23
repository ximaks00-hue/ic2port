package dev.ic2port.tube;

import dev.ic2port.blockentity.TubeBlockEntity;
import dev.ic2port.util.TubeRoutingHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Direction resolution and transfer routing for tube segments.
 */
public final class TubeRoutingService {

    private TubeRoutingService() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static List<Direction> resolveDirections(
            final TubeBlockEntity tube,
            final TransportedItem item,
            final BlockState state) {
        TubeRole role = tube.getCachedRole();
        if (role == TubeRole.SORTING) {
            return TubeRoutingHelper.sortingDirections(
                    item.getEntryDirection(),
                    tube.getPushPriorityIndex(),
                    item.getStack(),
                    tube.getSideFilters());
        }
        if (role == TubeRole.FILTER) {
            return TubeRoutingHelper.filterDirections(
                    item.getEntryDirection(),
                    tube.getPushPriorityIndex(),
                    item.getStack(),
                    tube.getFilterHandler(),
                    tube.getInventoryFacing());
        }
        if (role == TubeRole.COLOR_FILTER) {
            return TubeRoutingHelper.colorFilterDirections(
                    item.getEntryDirection(),
                    tube.getPushPriorityIndex(),
                    item.getColor(),
                    tube.getColorRoutes());
        }
        if (role == TubeRole.ROUND_ROBIN) {
            return TubeRoutingHelper.roundRobinDirection(item.getEntryDirection(), tube.getPushPriorityIndex());
        }
        if (role == TubeRole.INSERTION) {
            return TubeRoutingHelper.insertionDirections(
                    item.getEntryDirection(),
                    tube.getPushPriorityIndex(),
                    tube.getLevel(),
                    tube.getBlockPos(),
                    state);
        }
        if (item.getExportDirection() != null) {
            return List.of(item.getExportDirection());
        }
        Direction outputPriority = tube.getOutputPriority();
        if (outputPriority != null
                && (item.getEntryDirection() == null || outputPriority != item.getEntryDirection())) {
            List<Direction> directions = new ArrayList<>();
            directions.add(outputPriority);
            for (Direction direction : TubeRoutingHelper.pushDirections(
                    item.getEntryDirection(), tube.getPushPriorityIndex())) {
                if (direction != outputPriority) {
                    directions.add(direction);
                }
            }
            return directions;
        }
        return TubeRoutingHelper.pushDirections(item.getEntryDirection(), tube.getPushPriorityIndex());
    }
}
