package dev.ic2port.setup;

import dev.ic2port.Reference;
import dev.ic2port.blockentity.BatBoxBlockEntity;
import dev.ic2port.blockentity.ConstructionFoamBlockEntity;
import dev.ic2port.blockentity.BlastInductionFurnaceBlockEntity;
import dev.ic2port.blockentity.CentrifugalExtractorBlockEntity;
import dev.ic2port.blockentity.CopperCableBlockEntity;
import dev.ic2port.blockentity.CreativeGeneratorBlockEntity;
import dev.ic2port.blockentity.CropHarvesterBlockEntity;
import dev.ic2port.blockentity.CropAnalyzerBlockEntity;
import dev.ic2port.blockentity.CropmatronBlockEntity;
import dev.ic2port.blockentity.CropSticksBlockEntity;
import dev.ic2port.blockentity.AdvancedSolarPanelBlockEntity;
import dev.ic2port.blockentity.DetectorCableBlockEntity;
import dev.ic2port.blockentity.HvSolarPanelBlockEntity;
import dev.ic2port.blockentity.SplitterCableBlockEntity;
import dev.ic2port.blockentity.AlloySmelterBlockEntity;
import dev.ic2port.blockentity.ElectrolyzerBlockEntity;
import dev.ic2port.blockentity.PatternReplicatorBlockEntity;
import dev.ic2port.blockentity.TeleporterBlockEntity;
import dev.ic2port.blockentity.TerraformerBlockEntity;
import dev.ic2port.blockentity.MinerBlockEntity;
import dev.ic2port.blockentity.OreWasherBlockEntity;
import dev.ic2port.blockentity.PumpBlockEntity;
import dev.ic2port.blockentity.EsuBlockEntity;
import dev.ic2port.blockentity.EVTransformerBlockEntity;
import dev.ic2port.blockentity.GlassFiberCableBlockEntity;
import dev.ic2port.blockentity.GoldCableBlockEntity;
import dev.ic2port.blockentity.HvCableBlockEntity;
import dev.ic2port.blockentity.HvCreativeGeneratorBlockEntity;
import dev.ic2port.blockentity.LVTransformerBlockEntity;
import dev.ic2port.blockentity.MFEBlockEntity;
import dev.ic2port.blockentity.MFSUBlockEntity;
import dev.ic2port.blockentity.MVTransformerBlockEntity;
import dev.ic2port.blockentity.CannerBlockEntity;
import dev.ic2port.blockentity.VacuumCannerBlockEntity;
import dev.ic2port.blockentity.CompressorBlockEntity;
import dev.ic2port.blockentity.ElectricFurnaceBlockEntity;
import dev.ic2port.blockentity.ExtractorBlockEntity;
import dev.ic2port.blockentity.InductionFurnaceBlockEntity;
import dev.ic2port.blockentity.MetalFormerBlockEntity;
import dev.ic2port.blockentity.BrewingBarrelBlockEntity;
import dev.ic2port.blockentity.ChargePadBlockEntity;
import dev.ic2port.blockentity.MaceratorBlockEntity;
import dev.ic2port.blockentity.RecyclerBlockEntity;
import dev.ic2port.blockentity.NuclearReactorBlockEntity;
import dev.ic2port.blockentity.MassFabricatorBlockEntity;
import dev.ic2port.blockentity.ThermalCentrifugeBlockEntity;
import dev.ic2port.blockentity.SolarPanelBlockEntity;
import dev.ic2port.blockentity.WaterMillBlockEntity;
import dev.ic2port.blockentity.WindMillBlockEntity;
import dev.ic2port.blockentity.FusionReactorBlockEntity;
import dev.ic2port.blockentity.FusionReactorValveBlockEntity;
import dev.ic2port.blockentity.GeothermalGeneratorBlockEntity;
import dev.ic2port.blockentity.SolidFuelGeneratorBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Central registry for all {@link BlockEntityType} instances of this mod.
 */
public final class BlockEntityRegistry {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Reference.MOD_ID);

    public static final RegistryObject<BlockEntityType<CreativeGeneratorBlockEntity>> CREATIVE_GENERATOR_BE =
            BLOCK_ENTITIES.register("creative_generator", () -> BlockEntityType.Builder
                    .of(CreativeGeneratorBlockEntity::new, BlockRegistry.CREATIVE_GENERATOR.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<HvCreativeGeneratorBlockEntity>> HV_CREATIVE_GENERATOR_BE =
            BLOCK_ENTITIES.register("hv_creative_generator", () -> BlockEntityType.Builder
                    .of(HvCreativeGeneratorBlockEntity::new, BlockRegistry.HV_CREATIVE_GENERATOR.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<CopperCableBlockEntity>> COPPER_CABLE_BE =
            BLOCK_ENTITIES.register("copper_cable", () -> BlockEntityType.Builder
                    .of(CopperCableBlockEntity::new, BlockRegistry.COPPER_CABLE.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<GoldCableBlockEntity>> GOLD_CABLE_BE =
            BLOCK_ENTITIES.register("gold_cable", () -> BlockEntityType.Builder
                    .of(GoldCableBlockEntity::new, BlockRegistry.GOLD_CABLE.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<HvCableBlockEntity>> HV_CABLE_BE =
            BLOCK_ENTITIES.register("hv_cable", () -> BlockEntityType.Builder
                    .of(HvCableBlockEntity::new, BlockRegistry.HV_CABLE.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<GlassFiberCableBlockEntity>> GLASS_FIBER_CABLE_BE =
            BLOCK_ENTITIES.register("glass_fiber_cable", () -> BlockEntityType.Builder
                    .of(GlassFiberCableBlockEntity::new, BlockRegistry.GLASS_FIBER_CABLE.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<MaceratorBlockEntity>> MACERATOR_BE =
            BLOCK_ENTITIES.register("macerator", () -> BlockEntityType.Builder
                    .of(MaceratorBlockEntity::new, BlockRegistry.MACERATOR.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<RecyclerBlockEntity>> RECYCLER_BE =
            BLOCK_ENTITIES.register("recycler", () -> BlockEntityType.Builder
                    .of(RecyclerBlockEntity::new, BlockRegistry.RECYCLER.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<ExtractorBlockEntity>> EXTRACTOR_BE =
            BLOCK_ENTITIES.register("extractor", () -> BlockEntityType.Builder
                    .of(ExtractorBlockEntity::new, BlockRegistry.EXTRACTOR.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<CentrifugalExtractorBlockEntity>> CENTRIFUGAL_EXTRACTOR_BE =
            BLOCK_ENTITIES.register("centrifugal_extractor", () -> BlockEntityType.Builder
                    .of(CentrifugalExtractorBlockEntity::new, BlockRegistry.CENTRIFUGAL_EXTRACTOR.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<CompressorBlockEntity>> COMPRESSOR_BE =
            BLOCK_ENTITIES.register("compressor", () -> BlockEntityType.Builder
                    .of(CompressorBlockEntity::new, BlockRegistry.COMPRESSOR.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<CannerBlockEntity>> CANNER_BE =
            BLOCK_ENTITIES.register("canner", () -> BlockEntityType.Builder
                    .of(CannerBlockEntity::new, BlockRegistry.CANNER.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<VacuumCannerBlockEntity>> VACUUM_CANNER_BE =
            BLOCK_ENTITIES.register("vacuum_canner", () -> BlockEntityType.Builder
                    .of(VacuumCannerBlockEntity::new, BlockRegistry.VACUUM_CANNER.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<ElectricFurnaceBlockEntity>> ELECTRIC_FURNACE_BE =
            BLOCK_ENTITIES.register("electric_furnace", () -> BlockEntityType.Builder
                    .of(ElectricFurnaceBlockEntity::new, BlockRegistry.ELECTRIC_FURNACE.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<InductionFurnaceBlockEntity>> INDUCTION_FURNACE_BE =
            BLOCK_ENTITIES.register("induction_furnace", () -> BlockEntityType.Builder
                    .of(InductionFurnaceBlockEntity::new, BlockRegistry.INDUCTION_FURNACE.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<BlastInductionFurnaceBlockEntity>> BLAST_INDUCTION_FURNACE_BE =
            BLOCK_ENTITIES.register("blast_induction_furnace", () -> BlockEntityType.Builder
                    .of(BlastInductionFurnaceBlockEntity::new, BlockRegistry.BLAST_INDUCTION_FURNACE.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<MetalFormerBlockEntity>> METAL_FORMER_BE =
            BLOCK_ENTITIES.register("metal_former", () -> BlockEntityType.Builder
                    .of(MetalFormerBlockEntity::new, BlockRegistry.METAL_FORMER.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<ChargePadBlockEntity>> CHARGE_PAD_BE =
            BLOCK_ENTITIES.register("charge_pad", () -> BlockEntityType.Builder
                    .of(ChargePadBlockEntity::new, BlockRegistry.CHARGE_PAD.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<ThermalCentrifugeBlockEntity>> THERMAL_CENTRIFUGE_BE =
            BLOCK_ENTITIES.register("thermal_centrifuge", () -> BlockEntityType.Builder
                    .of(ThermalCentrifugeBlockEntity::new, BlockRegistry.THERMAL_CENTRIFUGE.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<MassFabricatorBlockEntity>> MASS_FABRICATOR_BE =
            BLOCK_ENTITIES.register("mass_fabricator", () -> BlockEntityType.Builder
                    .of(MassFabricatorBlockEntity::new, BlockRegistry.MASS_FABRICATOR.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<SolidFuelGeneratorBlockEntity>> SOLID_FUEL_GENERATOR_BE =
            BLOCK_ENTITIES.register("solid_fuel_generator", () -> BlockEntityType.Builder
                    .of(SolidFuelGeneratorBlockEntity::new, BlockRegistry.SOLID_FUEL_GENERATOR.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<GeothermalGeneratorBlockEntity>> GEOTHERMAL_GENERATOR_BE =
            BLOCK_ENTITIES.register("geothermal_generator", () -> BlockEntityType.Builder
                    .of(GeothermalGeneratorBlockEntity::new, BlockRegistry.GEOTHERMAL_GENERATOR.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<SolarPanelBlockEntity>> SOLAR_PANEL_BE =
            BLOCK_ENTITIES.register("solar_panel", () -> BlockEntityType.Builder
                    .of(SolarPanelBlockEntity::new, BlockRegistry.SOLAR_PANEL.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<WindMillBlockEntity>> WIND_MILL_BE =
            BLOCK_ENTITIES.register("wind_mill", () -> BlockEntityType.Builder
                    .of(WindMillBlockEntity::new, BlockRegistry.WIND_MILL.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<WaterMillBlockEntity>> WATER_MILL_BE =
            BLOCK_ENTITIES.register("water_mill", () -> BlockEntityType.Builder
                    .of(WaterMillBlockEntity::new, BlockRegistry.WATER_MILL.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<BatBoxBlockEntity>> BATBOX_BE =
            BLOCK_ENTITIES.register("batbox", () -> BlockEntityType.Builder
                    .of(BatBoxBlockEntity::new, BlockRegistry.BATBOX.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<MFEBlockEntity>> MFE_BE =
            BLOCK_ENTITIES.register("mfe", () -> BlockEntityType.Builder
                    .of(MFEBlockEntity::new, BlockRegistry.MFE.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<MFSUBlockEntity>> MFSU_BE =
            BLOCK_ENTITIES.register("mfsu", () -> BlockEntityType.Builder
                    .of(MFSUBlockEntity::new, BlockRegistry.MFSU.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<HvSolarPanelBlockEntity>> HV_SOLAR_PANEL_BE =
            BLOCK_ENTITIES.register("hv_solar_panel", () -> BlockEntityType.Builder
                    .of(HvSolarPanelBlockEntity::new, BlockRegistry.HV_SOLAR_PANEL.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<DetectorCableBlockEntity>> DETECTOR_CABLE_BE =
            BLOCK_ENTITIES.register("detector_cable", () -> BlockEntityType.Builder
                    .of(DetectorCableBlockEntity::new, BlockRegistry.DETECTOR_CABLE.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<SplitterCableBlockEntity>> SPLITTER_CABLE_BE =
            BLOCK_ENTITIES.register("splitter_cable", () -> BlockEntityType.Builder
                    .of(SplitterCableBlockEntity::new, BlockRegistry.SPLITTER_CABLE.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<AdvancedSolarPanelBlockEntity>> ADVANCED_SOLAR_PANEL_BE =
            BLOCK_ENTITIES.register("advanced_solar_panel", () -> BlockEntityType.Builder
                    .of(AdvancedSolarPanelBlockEntity::new, BlockRegistry.ADVANCED_SOLAR_PANEL.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<TeleporterBlockEntity>> TELEPORTER_BE =
            BLOCK_ENTITIES.register("teleporter", () -> BlockEntityType.Builder
                    .of(TeleporterBlockEntity::new, BlockRegistry.TELEPORTER.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<TerraformerBlockEntity>> TERRAFORMER_BE =
            BLOCK_ENTITIES.register("terraformer", () -> BlockEntityType.Builder
                    .of(TerraformerBlockEntity::new, BlockRegistry.TERRAFORMER.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<PatternReplicatorBlockEntity>> PATTERN_REPLICATOR_BE =
            BLOCK_ENTITIES.register("pattern_replicator", () -> BlockEntityType.Builder
                    .of(PatternReplicatorBlockEntity::new, BlockRegistry.PATTERN_REPLICATOR.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<ElectrolyzerBlockEntity>> ELECTROLYZER_BE =
            BLOCK_ENTITIES.register("electrolyzer", () -> BlockEntityType.Builder
                    .of(ElectrolyzerBlockEntity::new, BlockRegistry.ELECTROLYZER.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<OreWasherBlockEntity>> ORE_WASHER_BE =
            BLOCK_ENTITIES.register("ore_washer", () -> BlockEntityType.Builder
                    .of(OreWasherBlockEntity::new, BlockRegistry.ORE_WASHER.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<AlloySmelterBlockEntity>> ALLOY_SMELTER_BE =
            BLOCK_ENTITIES.register("alloy_smelter", () -> BlockEntityType.Builder
                    .of(AlloySmelterBlockEntity::new, BlockRegistry.ALLOY_SMELTER.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<MinerBlockEntity>> MINER_BE =
            BLOCK_ENTITIES.register("miner", () -> BlockEntityType.Builder
                    .of(MinerBlockEntity::new, BlockRegistry.MINER.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<PumpBlockEntity>> PUMP_BE =
            BLOCK_ENTITIES.register("pump", () -> BlockEntityType.Builder
                    .of(PumpBlockEntity::new, BlockRegistry.PUMP.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<EsuBlockEntity>> ESU_BE =
            BLOCK_ENTITIES.register("esu", () -> BlockEntityType.Builder
                    .of(EsuBlockEntity::new, BlockRegistry.ESU.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<LVTransformerBlockEntity>> LV_TRANSFORMER_BE =
            BLOCK_ENTITIES.register("lv_transformer", () -> BlockEntityType.Builder
                    .of(LVTransformerBlockEntity::new, BlockRegistry.LV_TRANSFORMER.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<MVTransformerBlockEntity>> MV_TRANSFORMER_BE =
            BLOCK_ENTITIES.register("mv_transformer", () -> BlockEntityType.Builder
                    .of(MVTransformerBlockEntity::new, BlockRegistry.MV_TRANSFORMER.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<EVTransformerBlockEntity>> EV_TRANSFORMER_BE =
            BLOCK_ENTITIES.register("ev_transformer", () -> BlockEntityType.Builder
                    .of(EVTransformerBlockEntity::new, BlockRegistry.EV_TRANSFORMER.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<NuclearReactorBlockEntity>> NUCLEAR_REACTOR_BE =
            BLOCK_ENTITIES.register("nuclear_reactor", () -> BlockEntityType.Builder
                    .of(NuclearReactorBlockEntity::new, BlockRegistry.NUCLEAR_REACTOR.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<CropSticksBlockEntity>> CROP_STICKS_BE =
            BLOCK_ENTITIES.register("crop_sticks", () -> BlockEntityType.Builder
                    .of(CropSticksBlockEntity::new, BlockRegistry.CROP_STICKS.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<CropHarvesterBlockEntity>> CROP_HARVESTER_BE =
            BLOCK_ENTITIES.register("crop_harvester", () -> BlockEntityType.Builder
                    .of(CropHarvesterBlockEntity::new, BlockRegistry.CROP_HARVESTER.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<CropmatronBlockEntity>> CROPMATRON_BE =
            BLOCK_ENTITIES.register("cropmatron", () -> BlockEntityType.Builder
                    .of(CropmatronBlockEntity::new, BlockRegistry.CROPMATRON.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<CropAnalyzerBlockEntity>> CROP_ANALYZER_BE =
            BLOCK_ENTITIES.register("crop_analyzer", () -> BlockEntityType.Builder
                    .of(CropAnalyzerBlockEntity::new, BlockRegistry.CROP_ANALYZER.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<BrewingBarrelBlockEntity>> BREWING_BARREL_BE =
            BLOCK_ENTITIES.register("brewing_barrel", () -> BlockEntityType.Builder
                    .of(BrewingBarrelBlockEntity::new, BlockRegistry.BREWING_BARREL.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<ConstructionFoamBlockEntity>> CONSTRUCTION_FOAM_BE =
            BLOCK_ENTITIES.register("construction_foam", () -> BlockEntityType.Builder
                    .of(ConstructionFoamBlockEntity::new, BlockRegistry.CONSTRUCTION_FOAM.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<FusionReactorBlockEntity>> FUSION_REACTOR_BE =
            BLOCK_ENTITIES.register("fusion_reactor", () -> BlockEntityType.Builder
                    .of(FusionReactorBlockEntity::new, BlockRegistry.FUSION_REACTOR.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<FusionReactorValveBlockEntity>> FUSION_REACTOR_VALVE_BE =
            BLOCK_ENTITIES.register("fusion_reactor_valve", () -> BlockEntityType.Builder
                    .of(FusionReactorValveBlockEntity::new, BlockRegistry.FUSION_REACTOR_VALVE.get())
                    .build(null));

    private BlockEntityRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void register(final IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
