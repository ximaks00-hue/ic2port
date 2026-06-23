package dev.ic2port.fluid;

import dev.ic2port.blockentity.FluidPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Chunk-local fluid pipe network — distributes fluid between connected {@link FluidPipeBlockEntity} nodes.
 */
public final class FluidPipeNetwork {

    private FluidPipeNetwork() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Attempts to push fluid from {@code source} into the connected pipe network.
     *
     * @return amount moved in mB
     */
    public static int distribute(
            final Level level,
            final BlockPos source,
            final FluidStack fluid,
            final int maxTransfer) {
        if (fluid.isEmpty() || maxTransfer <= 0) {
            return 0;
        }

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(source);
        visited.add(source);

        int remaining = maxTransfer;
        FluidStack working = fluid.copy();
        working.setAmount(Math.min(working.getAmount(), maxTransfer));

        while (!queue.isEmpty() && remaining > 0) {
            BlockPos current = queue.poll();
            BlockEntity blockEntity = level.getBlockEntity(current);
            if (!(blockEntity instanceof FluidPipeBlockEntity pipe)) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = current.relative(direction);
                if (visited.contains(neighborPos)) {
                    continue;
                }
                BlockEntity neighbor = level.getBlockEntity(neighborPos);
                if (neighbor instanceof FluidPipeBlockEntity) {
                    visited.add(neighborPos);
                    queue.add(neighborPos);
                    continue;
                }
                if (neighbor == null) {
                    continue;
                }
                IFluidHandler handler = neighbor.getCapability(
                        net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER,
                        direction.getOpposite()).orElse(null);
                if (handler == null) {
                    continue;
                }
                int filled = handler.fill(working, IFluidHandler.FluidAction.EXECUTE);
                if (filled > 0) {
                    remaining -= filled;
                    working.setAmount(remaining);
                    pipe.onTransferred(filled);
                    if (remaining <= 0) {
                        return maxTransfer;
                    }
                }
            }
        }
        return maxTransfer - remaining;
    }
}
