package dev.ic2port.tube;

import dev.ic2port.block.BaseTubeBlock;
import dev.ic2port.blockentity.TubeBlockEntity;
import dev.ic2port.util.TubeConnectionHelper;
import dev.ic2port.util.TubeLogisticsRegistry;
import dev.ic2port.util.TubeTransferHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/**
 * Request-tube logistic grid updates.
 */
public final class TubeLogisticsService {

    private TubeLogisticsService() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void tickLogisticRequest(final TubeBlockEntity tube) {
        if (tube.getLevel() == null || tube.getLevel().isClientSide) {
            return;
        }
        if (tube.decrementLogisticsCooldown()) {
            return;
        }
        boolean active = needsRequestedItem(tube);
        TubeLogisticsRegistry.updateRequest(
                tube.getLevel(),
                tube.getBlockPos(),
                tube.getRequestFilter(),
                tube.getPaintColor(),
                active);
    }

    private static boolean needsRequestedItem(final TubeBlockEntity tube) {
        ItemStack requestFilter = tube.getRequestFilter();
        if (requestFilter.isEmpty() || tube.getLevel() == null) {
            return false;
        }
        Direction facing = tube.getInventoryFacing();
        if (facing == null) {
            return false;
        }
        var inventoryPos = tube.getBlockPos().relative(facing);
        IItemHandler handler = TubeConnectionHelper.getItemHandler(tube.getLevel(), inventoryPos, facing.getOpposite());
        if (handler == null) {
            return false;
        }
        ItemStack remainder = TubeTransferHelper.insertIntoHandler(handler, requestFilter.copyWithCount(1));
        return remainder.isEmpty();
    }
}
