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

    public static final ForgeConfigSpec.DoubleValue RECYCLER_SCRAP_CHANCE;
    public static final ForgeConfigSpec.DoubleValue RECYCLER_SCRAP_BOX_CHANCE;

    public static final ForgeConfigSpec.BooleanValue REACTOR_PROFILING_ENABLED;
    public static final ForgeConfigSpec.IntValue REACTOR_PROFILING_THRESHOLD_MS;
    public static final ForgeConfigSpec.BooleanValue CABLE_PROFILING_ENABLED;
    public static final ForgeConfigSpec.IntValue CABLE_PROFILING_THRESHOLD_MS;
    public static final ForgeConfigSpec.BooleanValue TUBE_PROFILING_ENABLED;
    public static final ForgeConfigSpec.IntValue TUBE_PROFILING_THRESHOLD_MS;
    public static final ForgeConfigSpec.BooleanValue GLOBAL_ENERGY_NET_ENABLED;

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
        GLOBAL_ENERGY_NET_ENABLED = builder
                .comment("Use IC2 Classic-style global energy net (v2): one level tick for all active cables "
                        + "instead of per-block cable tickers.")
                .define("globalEnergyNetEnabled", true);
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
        RECYCLER_SCRAP_CHANCE = builder
                .comment("Chance (0–1) that the recycler produces scrap from a valid input.")
                .defineInRange("recyclerScrapChance", 0.125D, 0.0D, 1.0D);
        RECYCLER_SCRAP_BOX_CHANCE = builder
                .comment("Chance (0–1) that the recycler produces a scrap box instead of loose scrap.")
                .defineInRange("recyclerScrapBoxChance", 0.01D, 0.0D, 1.0D);
        builder.pop();

        builder.push("debug");
        REACTOR_PROFILING_ENABLED = builder
                .comment("Log nuclear reactor ticks slower than the threshold (server performance tuning).")
                .define("reactorProfilingEnabled", false);
        REACTOR_PROFILING_THRESHOLD_MS = builder
                .comment("Minimum tick duration in milliseconds to log when reactor profiling is enabled.")
                .defineInRange("reactorProfilingThresholdMs", 5, 1, 100);
        CABLE_PROFILING_ENABLED = builder
                .comment("Log cable forward ticks slower than the threshold (server performance tuning).")
                .define("cableProfilingEnabled", false);
        CABLE_PROFILING_THRESHOLD_MS = builder
                .comment("Minimum tick duration in milliseconds to log when cable profiling is enabled.")
                .defineInRange("cableProfilingThresholdMs", 2, 1, 100);
        TUBE_PROFILING_ENABLED = builder
                .comment("Log tube server ticks slower than the threshold (server performance tuning).")
                .define("tubeProfilingEnabled", false);
        TUBE_PROFILING_THRESHOLD_MS = builder
                .comment("Minimum tick duration in milliseconds to log when tube profiling is enabled.")
                .defineInRange("tubeProfilingThresholdMs", 5, 1, 100);
        builder.pop();

        COMMON_SPEC = builder.build();
    }

    private ModConfig() {
        throw new UnsupportedOperationException("Utility class");
    }
}
