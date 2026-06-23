package dev.ic2port.tube.handler;

import dev.ic2port.block.BaseTubeBlock;
import dev.ic2port.blockentity.TubeBlockEntity;
import dev.ic2port.tube.TransportedItem;
import dev.ic2port.tube.TubeRole;
import dev.ic2port.tube.TubeRoleHandler;
import dev.ic2port.util.TubeConnectionHelper;
import dev.ic2port.util.TubeLogisticsRegistry;
import dev.ic2port.util.TubeRoutingHelper;
import dev.ic2port.util.TubeTransferHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;

/**
 * Fulfills logistic requests from connected inventories.
 */
public final class ProviderTubeHandler implements TubeRoleHandler {

    public static final ProviderTubeHandler INSTANCE = new ProviderTubeHandler();

    private ProviderTubeHandler() {
    }

    @Override
    public TubeRole role() {
        return TubeRole.PROVIDER;
    }

    public boolean tryFulfillLogisticRequest(final TubeBlockEntity tube, final BlockState state) {
        if (tube.getLevel() == null) {
            return false;
        }
        Direction facing = tube.getInventoryFacing();
        if (facing == null || !BaseTubeBlock.isConnected(state, facing)) {
            return false;
        }
        var inventoryPos = tube.getBlockPos().relative(facing);
        IItemHandler handler = TubeConnectionHelper.getItemHandler(tube.getLevel(), inventoryPos, facing.getOpposite());
        if (handler == null) {
            return false;
        }
        for (TubeLogisticsRegistry.RequestEntry request : TubeLogisticsRegistry.getRequests(tube.getLevel())) {
            if (request.requester().equals(tube.getBlockPos())) {
                continue;
            }
            ItemStack extracted = TubeTransferHelper.extractMatchingItem(handler, request.filter());
            if (extracted.isEmpty()) {
                continue;
            }
            TransportedItem transported = new TransportedItem(extracted, facing);
            if (request.color() != null) {
                transported.setColor(request.color());
            } else {
                TubeRoutingHelper.applyTubePaint(transported, tube.getPaintColor());
            }
            tube.getInFlightItems().add(transported);
            return true;
        }
        return false;
    }
}
