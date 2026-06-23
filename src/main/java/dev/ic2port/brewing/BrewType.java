package dev.ic2port.brewing;

/**
 * Active recipe mode for the IC2-style brewing barrel.
 */
public enum BrewType {
    NONE,
    BEER,
    RUM,
    WHISKY,
    POTION,
    TEA,
    COFFEE;

    public static BrewType fromIndex(final int index) {
        BrewType[] values = values();
        if (index < 0 || index >= values.length) {
            return NONE;
        }
        return values[index];
    }
}
