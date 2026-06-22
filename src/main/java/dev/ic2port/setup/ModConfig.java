package dev.ic2port.setup;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Common mod configuration — energy storage, overload behaviour, balance tuning and debug profiling.
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

    public static final ForgeConfigSpec.DoubleValue REACTOR_MAX_HEAT;
    public static final ForgeConfigSpec.DoubleValue REACTOR_HEAT_WARNING_RATIO;
    public static final ForgeConfigSpec.DoubleValue REACTOR_HEAT_RADIATION_RATIO;
    public static final ForgeConfigSpec.IntValue REACTOR_RADIATION_RADIUS;
    public static final ForgeConfigSpec.DoubleValue FUSION_HEAT_EU_PER_TICK;
    public static final ForgeConfigSpec.DoubleValue FUSION_LAVA_MULTIPLIER;

    public static final ForgeConfigSpec.BooleanValue REACTOR_PROFILING_ENABLED;
    public static final ForgeConfigSpec.IntValue REACTOR_PROFILING_THRESHOLD_MS;

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
                .defineInRange("batboxCapacity", 40_000.0D, 1_000.0D, 1_000_000.0D);
        MFE_CAPACITY = builder
                .comment("Internal EU buffer capacity of the MFE (IC2 Classic: 600k EU).")
                .defineInRange("mfeCapacity", 600_000.0D, 10_000.0D, 100_000_000.0D);
        MFSU_CAPACITY = builder
                .comment("Internal EU buffer capacity of the MFSU (IC2 Classic: 10M EU).")
                .defineInRange("mfsuCapacity", 10_000_000.0D, 100_000.0D, 1_000_000_000.0D);
        SOLID_FUEL_GENERATOR_CAPACITY = builder
                .comment("Internal EU buffer capacity of the solid fuel generator.")
                .defineInRange("solidFuelGeneratorCapacity", 4000.0D, 100.0D, 100_000.0D);
        builder.pop();

        builder.push("balance");
        REACTOR_MAX_HEAT = builder
                .comment("Hull heat threshold before fission reactor meltdown.")
                .defineInRange("reactorMaxHeat", 10_000.0D, 1_000.0D, 100_000.0D);
        REACTOR_HEAT_WARNING_RATIO = builder
                .comment("Fraction of max heat (0–1) at which nearby blocks may ignite.")
                .defineInRange("reactorHeatWarningRatio", 0.5D, 0.1D, 0.95D);
        REACTOR_HEAT_RADIATION_RATIO = builder
                .comment("Fraction of max heat (0–1) at which nearby players receive radiation.")
                .defineInRange("reactorHeatRadiationRatio", 0.75D, 0.2D, 0.99D);
        REACTOR_RADIATION_RADIUS = builder
                .comment("Block radius for reactor overheat radiation effect.")
                .defineInRange("reactorRadiationRadius", 10, 3, 32);
        FUSION_HEAT_EU_PER_TICK = builder
                .comment("EU consumed per tick while fusion reactor is heating up.")
                .defineInRange("fusionHeatEuPerTick", 128.0D, 16.0D, 2048.0D);
        FUSION_LAVA_MULTIPLIER = builder
                .comment("Multiplier applied to fusion lava production per cycle.")
                .defineInRange("fusionLavaMultiplier", 1.0D, 0.25D, 4.0D);
        builder.pop();

        builder.push("debug");
        REACTOR_PROFILING_ENABLED = builder
                .comment("Log nuclear reactor ticks slower than the threshold (server performance tuning).")
                .define("reactorProfilingEnabled", false);
        REACTOR_PROFILING_THRESHOLD_MS = builder
                .comment("Minimum tick duration in milliseconds to log when reactor profiling is enabled.")
                .defineInRange("reactorProfilingThresholdMs", 5, 1, 100);
        builder.pop();

        COMMON_SPEC = builder.build();
    }

    private ModConfig() {
        throw new UnsupportedOperationException("Utility class");
    }
}
