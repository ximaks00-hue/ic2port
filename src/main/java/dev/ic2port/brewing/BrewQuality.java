package dev.ic2port.brewing;

/**
 * IC2-style beer quality tiers stored on {@link dev.ic2port.item.BeerItem}.
 */
public enum BrewQuality {
    BREW,
    YOUNGSTER,
    BEER,
    ALE,
    DRAGONBLOOD,
    BAD;

    public static BrewQuality fromIndex(final int index) {
        BrewQuality[] values = values();
        if (index < 0 || index >= values.length) {
            return BEER;
        }
        return values[index];
    }
}
