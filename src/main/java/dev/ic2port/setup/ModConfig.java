package dev.ic2port.setup;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Common mod configuration — energy storage, overload behaviour and cable burnout.
 */
public final class ModConfig {

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec.DoubleValue EXPLOSION_BASE_RADIUS;
    public static final ForgeConfigSpec.BooleanValue CABLE_BURNOUT_ENABLED;
    public static final ForgeConfigSpec.DoubleValue CABLE_BURNOUT_CHANCE;
    public static final ForgeConfigSpec.DoubleValue BATBOX_CAPACITY;
    public static final ForgeConfigSpec.DoubleValue MFE_CAPACITY;
    public static final ForgeConfigSpec.DoubleValue MFSU_CAPACITY;
    public static final ForgeConfigSpec.DoubleValue SOLID_FUEL_GENERATOR_CAPACITY;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("overload");
        EXPLOSION_BASE_RADIUS = builder
                .comment("Base explosion radius when a machine is overloaded by excessive voltage.")
                .defineInRange("explosionBaseRadius", 2.0D, 0.5D, 16.0D);
        CABLE_BURNOUT_ENABLED = builder
                .comment("Whether LV cables burn out when exposed to higher voltage tiers.")
                .define("cableBurnoutEnabled", true);
        CABLE_BURNOUT_CHANCE = builder
                .comment("Chance (0.0–1.0) that an overloaded cable burns out when burnout is enabled.")
                .defineInRange("cableBurnoutChance", 1.0D, 0.0D, 1.0D);
        builder.pop();

        builder.push("energy");
        BATBOX_CAPACITY = builder
                .comment("Internal EU buffer capacity of the BatBox.")
                .defineInRange("batboxCapacity", 40000.0D, 1000.0D, 1_000_000.0D);
        MFE_CAPACITY = builder
                .comment("Internal EU buffer capacity of the MFE.")
                .defineInRange("mfeCapacity", 4_000_000.0D, 10_000.0D, 100_000_000.0D);
        MFSU_CAPACITY = builder
                .comment("Internal EU buffer capacity of the MFSU.")
                .defineInRange("mfsuCapacity", 40_000_000.0D, 100_000.0D, 1_000_000_000.0D);
        SOLID_FUEL_GENERATOR_CAPACITY = builder
                .comment("Internal EU buffer capacity of the solid fuel generator.")
                .defineInRange("solidFuelGeneratorCapacity", 4000.0D, 100.0D, 100_000.0D);
        builder.pop();

        COMMON_SPEC = builder.build();
    }

    private ModConfig() {
        throw new UnsupportedOperationException("Utility class");
    }
}
