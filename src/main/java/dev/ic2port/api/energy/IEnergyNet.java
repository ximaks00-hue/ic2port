package dev.ic2port.api.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Per-dimension EU cable graph — Classic-style global energy net (v2).
 * <p>
 * Obtain via {@link EnergyNet#get(Level)}.
 */
public interface IEnergyNet {

    /**
     * @return cables registered in this dimension's energy net
     */
    int getRegisteredCableCount();

    /**
     * @return cables that carried or buffered EU on the last net tick
     */
    int getActiveCableCount();

    /**
     * @return connected grid id for a conductor position, or {@code -1} if unknown
     */
    int getGridId(BlockPos pos);

    /**
     * Marks the grid containing {@code pos} stale so topology and acceptor masks rebuild.
     */
    void invalidateGrid(BlockPos pos);

    /**
     * Access the energy net for a level, or {@code null} on the client.
     */
  @Nullable
    static IEnergyNet get(final Level level) {
        return EnergyNet.get(level);
    }
}
