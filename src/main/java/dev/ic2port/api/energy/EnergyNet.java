package dev.ic2port.api.energy;

import dev.ic2port.energy.WorldEnergyNet;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Entry point for the global EU energy net (v2).
 */
public final class EnergyNet {

    private EnergyNet() {
        throw new UnsupportedOperationException("Utility class");
    }

    @Nullable
    public static IEnergyNet get(final Level level) {
        if (level == null || level.isClientSide()) {
            return null;
        }
        return WorldEnergyNet.get(level);
    }
}
