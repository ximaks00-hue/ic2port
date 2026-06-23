/**
 * Deferred registry managers and mod bootstrap wiring.
 */
package dev.ic2port.setup;

import dev.ic2port.Reference;
import dev.ic2port.block.BatBoxBlock;
import dev.ic2port.block.BlastInductionFurnaceBlock;
import dev.ic2port.block.CentrifugalExtractorBlock;
import dev.ic2port.block.CopperCableBlock;
import dev.ic2port.block.ColorFilterTubeBlock;
import dev.ic2port.block.ExtractionTubeBlock;
import dev.ic2port.block.FilteredExtractionTubeBlock;
import dev.ic2port.block.FilterTubeBlock;
import dev.ic2port.block.HoverTubeBlock;
import dev.ic2port.block.ItemTubeBlock;
import dev.ic2port.block.LimiterTubeBlock;
import dev.ic2port.block.PickupTubeBlock;
import dev.ic2port.block.ProviderTubeBlock;
import dev.ic2port.block.InsertionTubeBlock;
import dev.ic2port.block.StickyTubeBlock;
import dev.ic2port.block.RedstoneTubeBlock;
import dev.ic2port.block.RoundRobinTubeBlock;
import dev.ic2port.block.StackingTubeBlock;
import dev.ic2port.block.SwitchTubeBlock;
import dev.ic2port.block.TeleportTubeBlock;
import dev.ic2port.block.TransportTubeBlock;
import dev.ic2port.block.VoidTubeBlock;
import dev.ic2port.block.RequestTubeBlock;
import dev.ic2port.block.SortingTubeBlock;
import dev.ic2port.block.SpeedTubeBlock;
import dev.ic2port.block.CannerBlock;
import dev.ic2port.block.CompressorBlock;
import dev.ic2port.block.ContaminatedSoilBlock;
import dev.ic2port.block.CreativeGeneratorBlock;
import dev.ic2port.block.ElectricFurnaceBlock;
import dev.ic2port.block.ExtractorBlock;
import dev.ic2port.block.GoldCableBlock;
import dev.ic2port.block.EVTransformerBlock;
import dev.ic2port.block.GlassFiberCableBlock;
import dev.ic2port.block.HvCableBlock;
import dev.ic2port.block.HvCreativeGeneratorBlock;
import dev.ic2port.block.InductionFurnaceBlock;
import dev.ic2port.block.IronScaffoldBlock;
import dev.ic2port.block.WoodenScaffoldBlock;
import dev.ic2port.block.LVTransformerBlock;
import dev.ic2port.block.AdvancedSolarPanelBlock;
import dev.ic2port.block.DetectorCableBlock;
import dev.ic2port.block.HvSolarPanelBlock;
import dev.ic2port.block.SplitterCableBlock;
import dev.ic2port.block.AlloySmelterBlock;
import dev.ic2port.block.ElectrolyzerBlock;
import dev.ic2port.block.PatternReplicatorBlock;
import dev.ic2port.block.TeleporterBlock;
import dev.ic2port.block.TerraformerBlock;
import dev.ic2port.block.MinerBlock;
import dev.ic2port.block.OreWasherBlock;
import dev.ic2port.block.PumpBlock;
import dev.ic2port.block.EsuBlock;
import dev.ic2port.block.PesuBlock;
import dev.ic2port.block.IsuBlock;
import dev.ic2port.block.OreScannerBlock;
import dev.ic2port.block.UuCropLibraryBlock;
import dev.ic2port.block.UuExpansionBlock;
import dev.ic2port.block.MFEBlock;
import dev.ic2port.block.MFSUBlock;
import dev.ic2port.block.MVTransformerBlock;
import dev.ic2port.block.MaceratorBlock;
import dev.ic2port.block.MachineBufferBlock;
import dev.ic2port.block.PersonalChestBlock;
import dev.ic2port.block.PersonalTankBlock;
import dev.ic2port.block.TradeOMatBlock;
import dev.ic2port.block.VillagerOMatBlock;
import dev.ic2port.block.ElectricEnchanterBlock;
import dev.ic2port.block.RecyclerBlock;
import dev.ic2port.block.BrewingBarrelBlock;
import dev.ic2port.block.ConstructionFoamBlock;
import dev.ic2port.block.CropAnalyzerBlock;
import dev.ic2port.block.CropHarvesterBlock;
import dev.ic2port.block.CropmatronBlock;
import dev.ic2port.block.CropSticksBlock;
import dev.ic2port.block.ReinforcedDoorBlock;
import dev.ic2port.block.VacuumCannerBlock;
import dev.ic2port.block.WetConstructionFoamBlock;
import dev.ic2port.block.ReinforcedGlassBlock;
import dev.ic2port.block.ReinforcedPlanksBlock;
import dev.ic2port.block.ReinforcedStoneBlock;
import dev.ic2port.block.RubberLeavesBlock;
import dev.ic2port.block.RubberSaplingBlock;
import dev.ic2port.block.RubberWoodBlock;
import dev.ic2port.block.NuclearReactorBlock;
import dev.ic2port.block.ReactorChamberBlock;
import dev.ic2port.block.MassFabricatorBlock;
import dev.ic2port.block.MetalFormerBlock;
import dev.ic2port.block.ChargePadBlock;
import dev.ic2port.block.ThermalCentrifugeBlock;
import dev.ic2port.block.FusionReactorBlock;
import dev.ic2port.block.FusionReactorValveBlock;
import dev.ic2port.block.GeothermalGeneratorBlock;
import dev.ic2port.block.SolidFuelGeneratorBlock;
import dev.ic2port.block.SolarPanelBlock;
import dev.ic2port.block.FluidNetworkPumpBlock;
import dev.ic2port.block.FluidOMatBlock;
import dev.ic2port.block.FluidPipeBlock;
import dev.ic2port.block.InductionMatrixBlock;
import dev.ic2port.block.InductionMatrixCasingBlock;
import dev.ic2port.block.SteamChamberBlock;
import dev.ic2port.block.SteamReactorBlock;
import dev.ic2port.block.VoidPipeBlock;
import dev.ic2port.block.WaterMillBlock;
import dev.ic2port.block.WindMillBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Central registry for all {@link Block} instances of this mod.
 */
public final class BlockRegistry {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.MOD_ID);

    public static final RegistryObject<Block> CREATIVE_GENERATOR = BLOCKS.register("creative_generator",
            () -> new CreativeGeneratorBlock(machineProperties(MapColor.COLOR_ORANGE)));

    public static final RegistryObject<Block> HV_CREATIVE_GENERATOR = BLOCKS.register("hv_creative_generator",
            () -> new HvCreativeGeneratorBlock(machineProperties(MapColor.FIRE)));

    public static final RegistryObject<Block> COPPER_CABLE = BLOCKS.register("copper_cable",
            () -> new CopperCableBlock(machineProperties(MapColor.COLOR_ORANGE).noOcclusion()));

    public static final RegistryObject<Block> GOLD_CABLE = BLOCKS.register("gold_cable",
            () -> new GoldCableBlock(machineProperties(MapColor.GOLD).noOcclusion()));

    public static final RegistryObject<Block> HV_CABLE = BLOCKS.register("hv_cable",
            () -> new HvCableBlock(machineProperties(MapColor.METAL).noOcclusion()));

    public static final RegistryObject<Block> GLASS_FIBER_CABLE = BLOCKS.register("glass_fiber_cable",
            () -> new GlassFiberCableBlock(machineProperties(MapColor.QUARTZ).noOcclusion()));

    public static final RegistryObject<Block> ITEM_TUBE = BLOCKS.register("item_tube", ItemTubeBlock::new);

    public static final RegistryObject<Block> EXTRACTION_TUBE = BLOCKS.register("extraction_tube", ExtractionTubeBlock::new);

    public static final RegistryObject<Block> REQUEST_TUBE = BLOCKS.register("request_tube", RequestTubeBlock::new);

    public static final RegistryObject<Block> LIMITER_TUBE = BLOCKS.register("limiter_tube", LimiterTubeBlock::new);

    public static final RegistryObject<Block> SORTING_TUBE = BLOCKS.register("sorting_tube", SortingTubeBlock::new);

    public static final RegistryObject<Block> FILTER_TUBE = BLOCKS.register("filter_tube", FilterTubeBlock::new);

    public static final RegistryObject<Block> SPEED_TUBE = BLOCKS.register("speed_tube", SpeedTubeBlock::new);

    public static final RegistryObject<Block> COLOR_FILTER_TUBE = BLOCKS.register("color_filter_tube", ColorFilterTubeBlock::new);

    public static final RegistryObject<Block> HOVER_TUBE = BLOCKS.register("hover_tube", HoverTubeBlock::new);

    public static final RegistryObject<Block> FILTERED_EXTRACTION_TUBE = BLOCKS.register(
            "filtered_extraction_tube", FilteredExtractionTubeBlock::new);

    public static final RegistryObject<Block> PICKUP_TUBE = BLOCKS.register("pickup_tube", PickupTubeBlock::new);

    public static final RegistryObject<Block> REDSTONE_TUBE = BLOCKS.register("redstone_tube", RedstoneTubeBlock::new);

    public static final RegistryObject<Block> SWITCH_TUBE = BLOCKS.register("switch_tube", SwitchTubeBlock::new);

    public static final RegistryObject<Block> TRANSPORT_TUBE = BLOCKS.register("transport_tube", TransportTubeBlock::new);

    public static final RegistryObject<Block> ROUND_ROBIN_TUBE = BLOCKS.register("round_robin_tube", RoundRobinTubeBlock::new);

    public static final RegistryObject<Block> STACKING_TUBE = BLOCKS.register("stacking_tube", StackingTubeBlock::new);

    public static final RegistryObject<Block> TELEPORT_TUBE = BLOCKS.register("teleport_tube", TeleportTubeBlock::new);

    public static final RegistryObject<Block> PROVIDER_TUBE = BLOCKS.register("provider_tube", ProviderTubeBlock::new);

    public static final RegistryObject<Block> INSERTION_TUBE = BLOCKS.register("insertion_tube", InsertionTubeBlock::new);

    public static final RegistryObject<Block> STICKY_TUBE = BLOCKS.register("sticky_tube", StickyTubeBlock::new);

    public static final RegistryObject<Block> VOID_TUBE = BLOCKS.register("void_tube", VoidTubeBlock::new);

    public static final RegistryObject<Block> MACHINE_BUFFER = BLOCKS.register("machine_buffer",
            () -> new MachineBufferBlock(machineProperties(MapColor.METAL)));

    public static final RegistryObject<Block> VILLAGER_O_MAT = BLOCKS.register("villager_o_mat",
            () -> new VillagerOMatBlock(machineProperties(MapColor.QUARTZ)));

    public static final RegistryObject<Block> PERSONAL_CHEST = BLOCKS.register("personal_chest",
            () -> new PersonalChestBlock(machineProperties(MapColor.WOOD)));

    public static final RegistryObject<Block> PERSONAL_TANK = BLOCKS.register("personal_tank",
            () -> new PersonalTankBlock(machineProperties(MapColor.WOOD)));

    public static final RegistryObject<Block> TRADE_O_MAT = BLOCKS.register("trade_o_mat",
            () -> new TradeOMatBlock(machineProperties(MapColor.QUARTZ)));

    public static final RegistryObject<Block> ELECTRIC_ENCHANTER = BLOCKS.register("electric_enchanter",
            () -> new ElectricEnchanterBlock(machineProperties(MapColor.COLOR_PURPLE)));

    public static final RegistryObject<Block> MACERATOR = BLOCKS.register("macerator",
            () -> new MaceratorBlock(machineProperties(MapColor.METAL)));

    public static final RegistryObject<Block> RECYCLER = BLOCKS.register("recycler",
            () -> new RecyclerBlock(machineProperties(MapColor.COLOR_LIGHT_BLUE)));

    public static final RegistryObject<Block> SOLID_FUEL_GENERATOR = BLOCKS.register("solid_fuel_generator",
            () -> new SolidFuelGeneratorBlock(machineProperties(MapColor.STONE)));

    public static final RegistryObject<Block> GEOTHERMAL_GENERATOR = BLOCKS.register("geothermal_generator",
            () -> new GeothermalGeneratorBlock(machineProperties(MapColor.METAL)));

    public static final RegistryObject<Block> SOLAR_PANEL = BLOCKS.register("solar_panel",
            () -> new SolarPanelBlock(machineProperties(MapColor.METAL)));

    public static final RegistryObject<Block> ADVANCED_SOLAR_PANEL = BLOCKS.register("advanced_solar_panel",
            () -> new AdvancedSolarPanelBlock(machineProperties(MapColor.METAL)));

    public static final RegistryObject<Block> HV_SOLAR_PANEL = BLOCKS.register("hv_solar_panel",
            () -> new HvSolarPanelBlock(machineProperties(MapColor.METAL)));

    public static final RegistryObject<Block> DETECTOR_CABLE = BLOCKS.register("detector_cable",
            () -> new DetectorCableBlock(machineProperties(MapColor.METAL).noOcclusion()));

    public static final RegistryObject<Block> SPLITTER_CABLE = BLOCKS.register("splitter_cable",
            () -> new SplitterCableBlock(machineProperties(MapColor.METAL).noOcclusion()));

    public static final RegistryObject<Block> TELEPORTER = BLOCKS.register("teleporter",
            () -> new TeleporterBlock(machineProperties(MapColor.COLOR_PURPLE)));

    public static final RegistryObject<Block> TERRAFORMER = BLOCKS.register("terraformer",
            () -> new TerraformerBlock(machineProperties(MapColor.COLOR_GREEN)));

    public static final RegistryObject<Block> PATTERN_REPLICATOR = BLOCKS.register("pattern_replicator",
            () -> new PatternReplicatorBlock(machineProperties(MapColor.DIAMOND)));

    public static final RegistryObject<Block> ELECTROLYZER = BLOCKS.register("electrolyzer",
            () -> new ElectrolyzerBlock(machineProperties(MapColor.COLOR_BLUE)));

    public static final RegistryObject<Block> ORE_WASHER = BLOCKS.register("ore_washer",
            () -> new OreWasherBlock(machineProperties(MapColor.COLOR_LIGHT_BLUE)));

    public static final RegistryObject<Block> ALLOY_SMELTER = BLOCKS.register("alloy_smelter",
            () -> new AlloySmelterBlock(machineProperties(MapColor.TERRACOTTA_ORANGE)));

    public static final RegistryObject<Block> MINER = BLOCKS.register("miner",
            () -> new MinerBlock(machineProperties(MapColor.METAL)));

    public static final RegistryObject<Block> PUMP = BLOCKS.register("pump",
            () -> new PumpBlock(machineProperties(MapColor.METAL)));

    public static final RegistryObject<Block> WIND_MILL = BLOCKS.register("wind_mill",
            () -> new WindMillBlock(machineProperties(MapColor.METAL)));

    public static final RegistryObject<Block> WATER_MILL = BLOCKS.register("water_mill",
            () -> new WaterMillBlock(machineProperties(MapColor.WOOD)));

    public static final RegistryObject<Block> BATBOX = BLOCKS.register("batbox",
            () -> new BatBoxBlock(machineProperties(MapColor.WOOD)));

    public static final RegistryObject<Block> MFE = BLOCKS.register("mfe",
            () -> new MFEBlock(machineProperties(MapColor.STONE)));

    public static final RegistryObject<Block> MFSU = BLOCKS.register("mfsu",
            () -> new MFSUBlock(machineProperties(MapColor.LAPIS)));

    public static final RegistryObject<Block> ESU = BLOCKS.register("esu",
            () -> new EsuBlock(machineProperties(MapColor.METAL)));

    public static final RegistryObject<Block> PESU = BLOCKS.register("pesu",
            () -> new PesuBlock(machineProperties(MapColor.COLOR_PURPLE)));

    public static final RegistryObject<Block> ISU = BLOCKS.register("isu",
            () -> new IsuBlock(machineProperties(MapColor.COLOR_CYAN)));

    public static final RegistryObject<Block> ORE_SCANNER = BLOCKS.register("ore_scanner",
            () -> new OreScannerBlock(machineProperties(MapColor.COLOR_GRAY)));

    public static final RegistryObject<Block> UU_CROP_LIBRARY = BLOCKS.register("uu_crop_library",
            () -> new UuCropLibraryBlock(machineProperties(MapColor.COLOR_GREEN)));

    public static final RegistryObject<Block> UU_EXPANSION = BLOCKS.register("uu_expansion",
            () -> new UuExpansionBlock(machineProperties(MapColor.COLOR_MAGENTA)));

    public static final RegistryObject<Block> LV_TRANSFORMER = BLOCKS.register("lv_transformer",
            () -> new LVTransformerBlock(machineProperties(MapColor.STONE)));

    public static final RegistryObject<Block> MV_TRANSFORMER = BLOCKS.register("mv_transformer",
            () -> new MVTransformerBlock(machineProperties(MapColor.DEEPSLATE)));

    public static final RegistryObject<Block> EV_TRANSFORMER = BLOCKS.register("ev_transformer",
            () -> new EVTransformerBlock(machineProperties(MapColor.COLOR_BLACK)));

    public static final RegistryObject<Block> TIN_ORE = BLOCKS.register("tin_ore",
            () -> new Block(oreProperties(MapColor.METAL, 3.0F, 3.0F)));

    public static final RegistryObject<Block> DEEPSLATE_TIN_ORE = BLOCKS.register("deepslate_tin_ore",
            () -> new Block(oreProperties(MapColor.DEEPSLATE, 4.5F, 3.0F)));

    public static final RegistryObject<Block> URANIUM_ORE = BLOCKS.register("uranium_ore",
            () -> new Block(oreProperties(MapColor.COLOR_LIGHT_GREEN, 4.0F, 3.0F)));

    public static final RegistryObject<Block> DEEPSLATE_URANIUM_ORE = BLOCKS.register("deepslate_uranium_ore",
            () -> new Block(oreProperties(MapColor.TERRACOTTA_GREEN, 4.5F, 3.0F)));

    public static final RegistryObject<Block> EXTRACTOR = BLOCKS.register("extractor",
            () -> new ExtractorBlock(machineProperties(MapColor.COLOR_GREEN)));

    public static final RegistryObject<Block> CENTRIFUGAL_EXTRACTOR = BLOCKS.register("centrifugal_extractor",
            () -> new CentrifugalExtractorBlock(machineProperties(MapColor.COLOR_LIGHT_BLUE)));

    public static final RegistryObject<Block> COMPRESSOR = BLOCKS.register("compressor",
            () -> new CompressorBlock(machineProperties(MapColor.COLOR_LIGHT_GRAY)));

    public static final RegistryObject<Block> CANNER = BLOCKS.register("canner",
            () -> new CannerBlock(machineProperties(MapColor.COLOR_ORANGE)));

    public static final RegistryObject<Block> VACUUM_CANNER = BLOCKS.register("vacuum_canner",
            () -> new VacuumCannerBlock(machineProperties(MapColor.COLOR_PURPLE)));

    public static final RegistryObject<Block> ELECTRIC_FURNACE = BLOCKS.register("electric_furnace",
            () -> new ElectricFurnaceBlock(machineProperties(MapColor.COLOR_BROWN)));

    public static final RegistryObject<Block> INDUCTION_FURNACE = BLOCKS.register("induction_furnace",
            () -> new InductionFurnaceBlock(machineProperties(MapColor.TERRACOTTA_ORANGE)));

    public static final RegistryObject<Block> BLAST_INDUCTION_FURNACE = BLOCKS.register("blast_induction_furnace",
            () -> new BlastInductionFurnaceBlock(machineProperties(MapColor.TERRACOTTA_RED)));

    public static final RegistryObject<Block> METAL_FORMER = BLOCKS.register("metal_former",
            () -> new MetalFormerBlock(machineProperties(MapColor.COLOR_GRAY)));

    public static final RegistryObject<Block> CHARGE_PAD = BLOCKS.register("charge_pad",
            () -> new ChargePadBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .noOcclusion()));

    public static final RegistryObject<Block> THERMAL_CENTRIFUGE = BLOCKS.register("thermal_centrifuge",
            () -> new ThermalCentrifugeBlock(machineProperties(MapColor.TERRACOTTA_CYAN)));

    public static final RegistryObject<Block> MASS_FABRICATOR = BLOCKS.register("mass_fabricator",
            () -> new MassFabricatorBlock(machineProperties(MapColor.COLOR_PURPLE)));

    public static final RegistryObject<Block> NUCLEAR_REACTOR = BLOCKS.register("nuclear_reactor",
            () -> new NuclearReactorBlock(machineProperties(MapColor.COLOR_GREEN)));

    public static final RegistryObject<Block> FUSION_REACTOR = BLOCKS.register("fusion_reactor",
            () -> new FusionReactorBlock(fusionReactorProperties()));

    public static final RegistryObject<Block> FUSION_REACTOR_VALVE = BLOCKS.register("fusion_reactor_valve",
            () -> new FusionReactorValveBlock(fusionValveProperties()));

    public static final RegistryObject<Block> REACTOR_CHAMBER = BLOCKS.register("reactor_chamber",
            () -> new ReactorChamberBlock());

    public static final RegistryObject<Block> STEAM_REACTOR = BLOCKS.register("steam_reactor",
            () -> new SteamReactorBlock(machineProperties(MapColor.COLOR_LIGHT_BLUE)));

    public static final RegistryObject<Block> STEAM_CHAMBER = BLOCKS.register("steam_chamber",
            () -> new SteamChamberBlock());

    public static final RegistryObject<Block> FLUID_PIPE = BLOCKS.register("fluid_pipe",
            () -> new FluidPipeBlock(machineProperties(MapColor.COLOR_BLUE).noOcclusion()));

    public static final RegistryObject<Block> FLUID_PUMP = BLOCKS.register("fluid_pump",
            () -> new FluidNetworkPumpBlock(machineProperties(MapColor.METAL)));

    public static final RegistryObject<Block> VOID_PIPE = BLOCKS.register("void_pipe",
            () -> new VoidPipeBlock(machineProperties(MapColor.COLOR_BLACK).noOcclusion()));

    public static final RegistryObject<Block> FLUID_O_MAT = BLOCKS.register("fluid_o_mat",
            () -> new FluidOMatBlock(machineProperties(MapColor.QUARTZ)));

    public static final RegistryObject<Block> INDUCTION_MATRIX = BLOCKS.register("induction_matrix",
            () -> new InductionMatrixBlock(machineProperties(MapColor.DIAMOND)));

    public static final RegistryObject<Block> INDUCTION_MATRIX_CASING = BLOCKS.register("induction_matrix_casing",
            () -> new InductionMatrixCasingBlock());

    public static final RegistryObject<Block> CONTAMINATED_SOIL = BLOCKS.register("contaminated_soil",
            () -> new ContaminatedSoilBlock());

    public static final RegistryObject<Block> BASIC_MACHINE_CASING = BLOCKS.register("basic_machine_casing",
            () -> new Block(machineProperties(MapColor.METAL)));

    public static final RegistryObject<Block> ADVANCED_MACHINE_CASING = BLOCKS.register("advanced_machine_casing",
            () -> new Block(machineProperties(MapColor.DIAMOND)));

    public static final RegistryObject<Block> RUBBER_WOOD = BLOCKS.register("rubber_wood",
            () -> new RubberWoodBlock(woodProperties(MapColor.PODZOL).randomTicks()));

    public static final RegistryObject<Block> RUBBER_SAPLING = BLOCKS.register("rubber_sapling",
            () -> new RubberSaplingBlock(saplingProperties()));

    public static final RegistryObject<Block> RUBBER_LEAVES = BLOCKS.register("rubber_leaves",
            () -> new RubberLeavesBlock(leavesProperties()));

    public static final RegistryObject<Block> CROP_STICKS = BLOCKS.register("crop_sticks",
            () -> new CropSticksBlock(cropSticksProperties()));

    public static final RegistryObject<Block> CROP_HARVESTER = BLOCKS.register("crop_harvester",
            () -> new CropHarvesterBlock(machineProperties(MapColor.COLOR_GREEN)));

    public static final RegistryObject<Block> CROPMATRON = BLOCKS.register("cropmatron",
            () -> new CropmatronBlock(machineProperties(MapColor.QUARTZ)));

    public static final RegistryObject<Block> CROP_ANALYZER = BLOCKS.register("crop_analyzer",
            () -> new CropAnalyzerBlock(machineProperties(MapColor.COLOR_CYAN)));

    public static final RegistryObject<Block> WET_CONSTRUCTION_FOAM = BLOCKS.register("wet_construction_foam",
            () -> new WetConstructionFoamBlock(foamProperties(0.4F, 0.5F)));

    public static final RegistryObject<Block> CONSTRUCTION_FOAM = BLOCKS.register("construction_foam",
            () -> new ConstructionFoamBlock(foamProperties(0.6F, 30.0F)));

    public static final RegistryObject<Block> REINFORCED_STONE = BLOCKS.register("reinforced_stone",
            () -> new ReinforcedStoneBlock(reinforcedStoneProperties()));

    public static final RegistryObject<Block> REINFORCED_GLASS = BLOCKS.register("reinforced_glass",
            () -> new ReinforcedGlassBlock(reinforcedGlassProperties()));

    public static final RegistryObject<Block> REINFORCED_DOOR = BLOCKS.register("reinforced_door",
            () -> new ReinforcedDoorBlock(reinforcedDoorProperties()));

    public static final RegistryObject<Block> REINFORCED_PLANKS = BLOCKS.register("reinforced_planks",
            () -> new ReinforcedPlanksBlock(reinforcedPlanksProperties()));

    public static final RegistryObject<Block> REINFORCED_BRICKS = BLOCKS.register("reinforced_bricks",
            () -> new ReinforcedStoneBlock(reinforcedStoneProperties()));

    public static final RegistryObject<Block> REINFORCED_COBBLESTONE = BLOCKS.register("reinforced_cobblestone",
            () -> new ReinforcedStoneBlock(reinforcedStoneProperties()));

    public static final RegistryObject<Block> REINFORCED_CRACKED_STONE = BLOCKS.register("reinforced_cracked_stone",
            () -> new ReinforcedStoneBlock(reinforcedStoneProperties()));

    public static final RegistryObject<Block> REINFORCED_CLEAR_GLASS = BLOCKS.register("reinforced_clear_glass",
            () -> new ReinforcedGlassBlock(reinforcedGlassProperties()));

    public static final RegistryObject<Block> WOODEN_SCAFFOLD = BLOCKS.register("wooden_scaffold",
            () -> new WoodenScaffoldBlock(woodenScaffoldProperties()));

    public static final RegistryObject<Block> IRON_SCAFFOLD = BLOCKS.register("iron_scaffold",
            () -> new IronScaffoldBlock(ironScaffoldProperties()));

    public static final RegistryObject<Block> BREWING_BARREL = BLOCKS.register("brewing_barrel",
            () -> new BrewingBarrelBlock(woodProperties(MapColor.WOOD)));

    private BlockRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static BlockBehaviour.Properties machineProperties(final MapColor mapColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(mapColor)
                .strength(3.0F, 6.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL);
    }

    private static BlockBehaviour.Properties oreProperties(
            final MapColor mapColor,
            final float hardness,
            final float resistance) {
        return BlockBehaviour.Properties.of()
                .mapColor(mapColor)
                .strength(hardness, resistance)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE);
    }

    private static BlockBehaviour.Properties woodProperties(final MapColor mapColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(mapColor)
                .strength(2.0F)
                .sound(SoundType.WOOD);
    }

    private static BlockBehaviour.Properties leavesProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .strength(0.2F)
                .randomTicks()
                .sound(SoundType.GRASS)
                .noOcclusion()
                .ignitedByLava();
    }

    private static BlockBehaviour.Properties cropSticksProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .noCollission()
                .instabreak()
                .randomTicks()
                .sound(SoundType.WOOD)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties saplingProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .noCollission()
                .randomTicks()
                .instabreak()
                .sound(SoundType.GRASS);
    }

    private static BlockBehaviour.Properties foamProperties(final float hardness, final float resistance) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.QUARTZ)
                .strength(hardness, resistance)
                .sound(SoundType.WOOL);
    }

    private static BlockBehaviour.Properties reinforcedStoneProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(80.0F, 1200.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.DEEPSLATE);
    }

    private static BlockBehaviour.Properties reinforcedDoorProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 800.0F)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .sound(SoundType.METAL);
    }

    private static BlockBehaviour.Properties reinforcedPlanksProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(5.0F, 120.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.WOOD);
    }

    private static BlockBehaviour.Properties reinforcedGlassProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.QUARTZ)
                .strength(0.3F, 800.0F)
                .noOcclusion()
                .requiresCorrectToolForDrops()
                .sound(SoundType.GLASS);
    }

    private static BlockBehaviour.Properties woodenScaffoldProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .noCollission()
                .instabreak()
                .dynamicShape()
                .sound(SoundType.BAMBOO);
    }

    private static BlockBehaviour.Properties ironScaffoldProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .noCollission()
                .strength(1.0F, 2.0F)
                .dynamicShape()
                .sound(SoundType.METAL);
    }

    private static BlockBehaviour.Properties fusionReactorProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.FIRE)
                .strength(5.0F, 1200.0F)
                .requiresCorrectToolForDrops()
                .lightLevel(state -> 8)
                .sound(SoundType.METAL);
    }

    private static BlockBehaviour.Properties fusionValveProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 800.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL);
    }

    public static void register(final IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
