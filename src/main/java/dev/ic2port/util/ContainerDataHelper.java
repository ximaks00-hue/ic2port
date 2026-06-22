package dev.ic2port.util;

/**
 * Server-authoritative {@link net.minecraft.world.inventory.ContainerData} slots must not
 * accept client writes via {@code set()} — use these helpers in no-op setters.
 */
public final class ContainerDataHelper {

    private ContainerDataHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    /** Marker for display-only container data slots owned by the server. */
    public static void ignoreClientWrite() {
        // no-op
    }
}
