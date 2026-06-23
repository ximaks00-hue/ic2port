package dev.ic2port.tube.handler;

import dev.ic2port.block.BaseTubeBlock;
import dev.ic2port.blockentity.TubeBlockEntity;
import dev.ic2port.tube.TransportedItem;
import dev.ic2port.tube.TubeRole;
import dev.ic2port.tube.TubeRoleHandler;
import dev.ic2port.util.TubeConnectionHelper;
import dev.ic2port.util.TubeRoutingHelper;
import dev.ic2port.util.TubeTransferHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Pulls items from a connected inventory into the tube network.
 */
public final class ExtractionTubeHandler implements TubeRoleHandler {

    public static final ExtractionTubeHandler INSTANCE = new ExtractionTubeHandler();

    private ExtractionTubeHandler() {
    }

    @Override
    public TubeRole role() {
        return TubeRole.EXTRACTION;
    }

    @Override
    public boolean supportsExtraction() {
        return true;
    }

    public boolean tryExtract(final TubeBlockEntity tube, final BlockState state) {
        int budget = tube.getCachedRole() == TubeRole.FILTERED_EXTRACTION ? 2 : 1;
        boolean extracted = false;
        while (budget > 0 && tube.getInFlightItems().size() < TubeBlockEntity.MAX_ITEMS) {
            boolean got = tryExtractFromDirection(tube, state, tube.getInventoryFacing());
            if (!got && tube.getExtraExtractDirection() != null) {
                got = tryExtractFromDirection(tube, state, tube.getExtraExtractDirection());
            }
            if (!got) {
                break;
            }
            extracted = true;
            budget--;
        }
        return extracted;
    }

    private boolean tryExtractFromDirection(
            final TubeBlockEntity tube,
            final BlockState state,
            @Nullable final Direction facing) {
        if (facing == null || !BaseTubeBlock.isConnected(state, facing) || tube.getLevel() == null) {
            return false;
        }
        var inventoryPos = tube.getBlockPos().relative(facing);
        IItemHandler handler = TubeConnectionHelper.getItemHandler(tube.getLevel(), inventoryPos, facing.getOpposite());
        ItemStack extracted = tube.getCachedRole() == TubeRole.FILTERED_EXTRACTION
                ? TubeTransferHelper.extractMatchingItem(handler, tube.getExtractionFilter())
                : TubeTransferHelper.extractOneItem(handler);
        if (extracted.isEmpty()) {
            return false;
        }
        TransportedItem transported = new TransportedItem(extracted, facing);
        TubeRoutingHelper.applyTubePaint(transported, tube.getPaintColor());
        tube.getInFlightItems().add(transported);
        return true;
    }
}
