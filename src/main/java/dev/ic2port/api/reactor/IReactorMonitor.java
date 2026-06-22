package dev.ic2port.api.reactor;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Public read-only contract for external monitoring of a nuclear reactor.
 * <p>
 * Intended for redstone automation today and optional OpenComputers callbacks in a later phase.
 */
public interface IReactorMonitor {

    /**
     * @return current reactor hull heat in HU
     */
    double getHeat();

    /**
     * @return maximum hull heat before meltdown in HU
     */
    double getMaxHeat();

    /**
     * @return EU currently buffered inside the reactor (stored + pending this tick)
     */
    double getProducedEnergy();

    /**
     * @return {@code true} when the reactor has entered a meltdown/ejection sequence
     */
    boolean isEjected();

    /**
     * @return IC2 energy tier used for EU output ({@link dev.ic2port.api.energy.EnergyTier})
     */
    int getOutputTier();

    /**
     * @return {@code true} when the reactor receives a redstone signal and may simulate fuel
     */
    boolean isActive();

    /**
     * @return comparator output 0–15 proportional to {@link #getHeat()} / {@link #getMaxHeat()}
     */
    default int getHeatComparatorOutput() {
        double maxHeat = getMaxHeat();
        if (maxHeat <= 0.0D) {
            return 0;
        }
        return Mth.clamp((int) (getHeat() * 15.0D / maxHeat), 0, 15);
    }

    @Nullable
    static IReactorMonitor getAt(final Level level, final BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof IReactorMonitor monitor) {
            return monitor;
        }
        return null;
    }
}
