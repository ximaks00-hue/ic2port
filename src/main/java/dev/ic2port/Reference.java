package dev.ic2port;

/**
 * Global mod constants. All string literals used across the mod must reference this class.
 */
public final class Reference {

    /** Unique mod identifier used in registries, resource locations and networking. */
    public static final String MOD_ID = "ic2port";

    /** Human-readable mod name displayed in logs and the mod list. */
    public static final String MOD_NAME = "IC2 Port";

    /** Current mod version. Kept in sync with {@code gradle.properties} ({@code mod_version}). */
    public static final String VERSION = "0.1.0-SNAPSHOT";

    private Reference() {
        throw new UnsupportedOperationException("Utility class");
    }
}
