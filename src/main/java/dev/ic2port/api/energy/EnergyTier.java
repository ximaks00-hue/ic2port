package dev.ic2port.api.energy;

/**
 * IC2 voltage tier reference values (max EU per tick per packet).
 */
public final class EnergyTier {

    public static final int LV = 1;
    public static final int MV = 2;
    public static final int HV = 3;
    public static final int EV = 4;

    public static final double LV_MAX_PACKET = 32.0D;
    public static final double MV_MAX_PACKET = 128.0D;
    public static final double HV_MAX_PACKET = 512.0D;
    public static final double EV_MAX_PACKET = 2048.0D;

    private EnergyTier() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * @return maximum EU/t packet size for the given tier
     */
    public static double maxPacketForTier(final int tier) {
        return switch (tier) {
            case LV -> LV_MAX_PACKET;
            case MV -> MV_MAX_PACKET;
            case HV -> HV_MAX_PACKET;
            case EV -> EV_MAX_PACKET;
            default -> LV_MAX_PACKET;
        };
    }
}
