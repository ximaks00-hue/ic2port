package dev.ic2port.api.crops;

/**
 * IC2-style crop property vector used for breeding metadata.
 */
public record CropProperties(
        int tier,
        int chemistry,
        int consumable,
        int defensive,
        int colorful,
        int weed) {
}
