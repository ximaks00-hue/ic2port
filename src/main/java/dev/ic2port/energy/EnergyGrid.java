package dev.ic2port.energy;

import dev.ic2port.util.EnergyTransferHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Connected component of EU conductors in a single dimension.
 */
final class EnergyGrid {

    private final int id;
    private final Set<BlockPos> members = new HashSet<>();
    private int combinedAcceptorMask = -1;

    EnergyGrid(final int id) {
        this.id = id;
    }

    int id() {
        return id;
    }

    Set<BlockPos> members() {
        return Collections.unmodifiableSet(members);
    }

    boolean addMember(final BlockPos pos) {
        combinedAcceptorMask = -1;
        return members.add(pos);
    }

    void removeMember(final BlockPos pos) {
        members.remove(pos);
        combinedAcceptorMask = -1;
    }

    boolean isEmpty() {
        return members.isEmpty();
    }

    boolean contains(final BlockPos pos) {
        return members.contains(pos);
    }

    void invalidateMask() {
        combinedAcceptorMask = -1;
    }

    int getAcceptorMask(final Level level) {
        if (combinedAcceptorMask < 0) {
            int mask = 0;
            for (BlockPos member : members) {
                mask |= EnergyTransferHelper.buildDirectNeighborAcceptorMask(level, member);
            }
            combinedAcceptorMask = mask;
        }
        return combinedAcceptorMask;
    }
}
