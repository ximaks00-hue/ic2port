package dev.ic2port.util;

import com.mojang.logging.LogUtils;
import dev.ic2port.setup.ModConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import org.slf4j.Logger;

/**
 * Optional server-side profiler for cable forwarding and tube logistics ticks (disabled by default).
 */
public final class TickProfiler {

    private static final Logger LOGGER = LogUtils.getLogger();

    private TickProfiler() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void profileCable(final Runnable work) {
        profile(
                "cable_forward",
                ModConfig.CABLE_PROFILING_ENABLED,
                ModConfig.CABLE_PROFILING_THRESHOLD_MS,
                work);
    }

    public static void profileTube(final Runnable work) {
        profile(
                "tube_serverTick",
                ModConfig.TUBE_PROFILING_ENABLED,
                ModConfig.TUBE_PROFILING_THRESHOLD_MS,
                work);
    }

    private static void profile(
            final String label,
            final ForgeConfigSpec.BooleanValue enabled,
            final ForgeConfigSpec.IntValue thresholdMs,
            final Runnable work) {
        if (!enabled.get()) {
            work.run();
            return;
        }
        final long start = System.nanoTime();
        work.run();
        final long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
        if (elapsedMs >= thresholdMs.get()) {
            LOGGER.warn("[ic2port] Slow tick ({}): {} ms", label, elapsedMs);
        }
    }
}
