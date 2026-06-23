package dev.ic2port.recipe;

import net.minecraft.util.StringRepresentable;

/**
 * Metal former operating mode — recipes only match when the machine mode equals the recipe mode.
 */
public enum MetalFormerMode implements StringRepresentable {
    ROLLING("rolling"),
    EXTRUDING("extruding"),
    CUTTING("cutting");

    public static final MetalFormerMode DEFAULT = ROLLING;

    private final String serializedName;

    MetalFormerMode(final String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public MetalFormerMode next() {
        return switch (this) {
            case ROLLING -> EXTRUDING;
            case EXTRUDING -> CUTTING;
            case CUTTING -> ROLLING;
        };
    }

    public static MetalFormerMode fromString(final String value) {
        for (final MetalFormerMode mode : values()) {
            if (mode.serializedName.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return DEFAULT;
    }
}
