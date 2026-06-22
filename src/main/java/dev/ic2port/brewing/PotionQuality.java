package dev.ic2port.brewing;

/**
 * IC2-style brewed potion quality tiers stored on {@link dev.ic2port.item.BrewedPotionItem}.
 */
public enum PotionQuality {
    RAW,
    UNREFINED,
    IMPURE,
    REDUCED,
    PURE,
    CONCENTRATED,
    BAD;

    public static PotionQuality fromIndex(final int index) {
        PotionQuality[] values = values();
        if (index < 0 || index >= values.length) {
            return IMPURE;
        }
        return values[index];
    }
}
