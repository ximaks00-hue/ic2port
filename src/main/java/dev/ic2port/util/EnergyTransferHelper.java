package dev.ic2port.util;

import dev.ic2port.blockentity.BaseCableBlockEntity;
import dev.ic2port.energy.WorldEnergyNet;
import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.api.energy.IEnergyNode;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.util.EuReaderFlowService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Server-side helpers for querying adjacent EU capabilities.
 */
public final class EnergyTransferHelper {

    private EnergyTransferHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * @param querySide side of the <em>target</em> block facing the source (i.e. {@code sourceDir.getOpposite()})
     */
    @Nullable
    public static IEnergyAcceptor getAcceptor(
            final Level level,
            final BlockPos targetPos,
            final Direction querySide) {
        BlockEntity blockEntity = level.getBlockEntity(targetPos);
        if (blockEntity == null) {
            return null;
        }

        IEnergyNode node = blockEntity.getCapability(ModCapabilities.ENERGY_NODE_CAPABILITY, querySide)
                .orElse(null);

        if (node instanceof IEnergyAcceptor acceptor) {
            return acceptor;
        }
        return null;
    }

    /**
     * Attempts to inject EU into the neighbor reached through {@code outgoingDirection}.
     *
     * @return remainder that was not accepted
     */
    public static double injectIntoNeighbor(
            final Level level,
            final BlockPos sourcePos,
            final Direction outgoingDirection,
            final double amount,
            final int tier) {
        if (amount <= 0.0D) {
            return 0.0D;
        }

        BlockPos targetPos = sourcePos.relative(outgoingDirection);
        Direction targetFacing = outgoingDirection.getOpposite();
        IEnergyAcceptor acceptor = getAcceptor(level, targetPos, targetFacing);
        if (acceptor == null) {
            return amount;
        }
        double remainder = acceptor.injectEnergy(targetFacing, amount, tier);
        double transferred = amount - remainder;
        if (transferred > 0.0D) {
            EuReaderFlowService.recordTransfer(level, sourcePos, targetPos, transferred);
        }
        return remainder;
    }

    /**
     * Forwards buffered cable EU to neighbors, consuming {@code transferLoss} from the packet first.
     *
     * @return EU remaining in the cable buffer after forwarding
     */
    public static double forwardCablePacket(
            final Level level,
            final BlockPos sourcePos,
            @Nullable final Direction blockedSide,
            final double storedEnergy,
            final double transferLoss,
            final int tier) {
        return forwardCablePacket(
                level, sourcePos, blockedSide, storedEnergy, transferLoss, tier, ALL_NEIGHBOR_FACES_MASK);
    }

    /**
     * Forwards buffered cable EU to neighbors on faces set in {@code neighborAcceptorMask}.
     *
     * @param neighborAcceptorMask bit mask of {@link #faceBit(Direction)}; only set faces are queried
     * @return EU remaining in the cable buffer after forwarding
     */
    public static double forwardCablePacket(
            final Level level,
            final BlockPos sourcePos,
            @Nullable final Direction blockedSide,
            final double storedEnergy,
            final double transferLoss,
            final int tier,
            final int neighborAcceptorMask) {
        if (storedEnergy <= transferLoss) {
            return 0.0D;
        }

        double remainder = storedEnergy - transferLoss;
        for (final Direction direction : Direction.values()) {
            if (direction == blockedSide || remainder <= 0.0D) {
                continue;
            }
            if ((neighborAcceptorMask & faceBit(direction)) == 0) {
                continue;
            }
            remainder = injectIntoNeighbor(level, sourcePos, direction, remainder, tier);
        }
        return remainder;
    }

    /**
     * Builds a bit mask of adjacent faces that expose an {@link IEnergyAcceptor}.
     * <p>
     * EU net v2: when the source is a cable, uses chunk-scoped cluster caching.
     */
    public static int buildNeighborAcceptorMask(final Level level, final BlockPos sourcePos) {
        if (level instanceof ServerLevel serverLevel && WorldEnergyNet.isEnabled()) {
            BlockEntity blockEntity = level.getBlockEntity(sourcePos);
            if (blockEntity instanceof BaseCableBlockEntity) {
                return WorldEnergyNet.get(serverLevel).getAcceptorMaskForCable(sourcePos);
            }
        }
        BlockEntity blockEntity = level.getBlockEntity(sourcePos);
        if (blockEntity instanceof BaseCableBlockEntity) {
            return EnergyCableClusterHelper.getClusterAcceptorMask(level, sourcePos);
        }
        return buildDirectNeighborAcceptorMask(level, sourcePos);
    }

    /**
     * Invalidates cached cable cluster data for the chunk containing {@code pos}.
     */
    public static void invalidateCableCluster(final Level level, final BlockPos pos) {
        EnergyCableClusterHelper.invalidateChunk(level, new ChunkPos(pos));
    }

    public static int buildDirectNeighborAcceptorMask(final Level level, final BlockPos sourcePos) {
        int mask = 0;
        for (final Direction direction : Direction.values()) {
            BlockPos targetPos = sourcePos.relative(direction);
            Direction targetFacing = direction.getOpposite();
            if (getAcceptor(level, targetPos, targetFacing) != null) {
                mask |= faceBit(direction);
            }
        }
        return mask;
    }

    public static int faceBit(final Direction direction) {
        return 1 << direction.ordinal();
    }

    private static final int ALL_NEIGHBOR_FACES_MASK =
            faceBit(Direction.DOWN)
                    | faceBit(Direction.UP)
                    | faceBit(Direction.NORTH)
                    | faceBit(Direction.SOUTH)
                    | faceBit(Direction.WEST)
                    | faceBit(Direction.EAST);
}
