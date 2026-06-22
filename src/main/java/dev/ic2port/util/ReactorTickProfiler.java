package dev.ic2port.util;

import com.mojang.logging.LogUtils;
import dev.ic2port.setup.ModConfig;
import org.slf4j.Logger;

/**
 * Optional server-side profiler for nuclear reactor ticks (disabled by default).
 */
public final class ReactorTickProfiler {

    private static final Logger LOGGER = LogUtils.getLogger();

    private ReactorTickProfiler() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void profile(final String label, final Runnable work) {
        if (!ModConfig.REACTOR_PROFILING_ENABLED.get()) {
            work.run();
            return;
        }
        final long start = System.nanoTime();
        work.run();
        final long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
        if (elapsedMs >= ModConfig.REACTOR_PROFILING_THRESHOLD_MS.get()) {
            LOGGER.warn("[ic2port] Slow reactor tick ({}): {} ms", label, elapsedMs);
        }
    }
}
