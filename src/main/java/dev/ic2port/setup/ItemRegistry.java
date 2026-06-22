package dev.ic2port.setup;

import dev.ic2port.Reference;
import dev.ic2port.item.AdvancedDrillItem;
import dev.ic2port.item.AutoFeederModuleItem;
import dev.ic2port.item.BatPackItem;
import dev.ic2port.item.EnergyCrystalItem;
import dev.ic2port.item.EnergyStorageUpgradeItem;
import dev.ic2port.item.LapPackItem;
import dev.ic2port.item.ChargePadItem;
import dev.ic2port.item.CentrifugeRotorItem;
import dev.ic2port.item.CropnalyzerItem;
import dev.ic2port.item.CropSeedItem;
import dev.ic2port.item.EuReaderItem;
import dev.ic2port.item.FertilizerItem;
import dev.ic2port.item.ElectricFoamSprayerItem;
import dev.ic2port.item.FilledTinCanItem;
import dev.ic2port.item.EnergyShieldModuleItem;
import dev.ic2port.item.FoodStorageModuleItem;
import dev.ic2port.item.JetpackModuleItem;
import dev.ic2port.item.LapotronEnergyPackModuleItem;
import dev.ic2port.item.PortableEnergyPackModuleItem;
import dev.ic2port.item.FoamSprayerItem;
import dev.ic2port.item.RumItem;
import dev.ic2port.item.HydrationCellItem;
import dev.ic2port.item.ObscuratorItem;
import dev.ic2port.item.TinCanItem;
import dev.ic2port.item.WhiskyItem;
import dev.ic2port.item.WeedExItem;
import dev.ic2port.item.MiningLaserItem;
import dev.ic2port.item.OdScannerItem;
import dev.ic2port.item.OvScannerItem;
import dev.ic2port.item.NanoSuitItem;
import dev.ic2port.item.DiamondDrillItem;
import dev.ic2port.item.ElectricJetpackItem;
import dev.ic2port.item.CoolantCellItem;
import dev.ic2port.item.DepletedFuelRodItem;
import dev.ic2port.item.DepletedUraniumItem;
import dev.ic2port.item.LapotronCrystalItem;
import dev.ic2port.item.MoxFuelRodItem;
import dev.ic2port.item.PlutoniumItem;
import dev.ic2port.item.IridiumItem;
import dev.ic2port.item.QuantumSuitItem;
import dev.ic2port.item.TerraWartItem;
import dev.ic2port.item.ScrapBoxItem;
import dev.ic2port.item.ScrapItem;
import dev.ic2port.item.UuMatterItem;
import dev.ic2port.item.BasicDrillItem;
import dev.ic2port.item.FluidCellItem;
import dev.ic2port.item.ChainsawItem;
import dev.ic2port.item.DualFuelRodItem;
import dev.ic2port.item.ElectricWrenchItem;
import dev.ic2port.item.FuelRodItem;
import dev.ic2port.item.HeatExchangerItem;
import dev.ic2port.item.HeatVentItem;
import dev.ic2port.item.NeutronReflectorItem;
import dev.ic2port.item.QuadFuelRodItem;
import dev.ic2port.item.ReactorCondensatorItem;
import dev.ic2port.item.ReactorPlatingItem;
import dev.ic2port.item.BronzeArmorItem;
import dev.ic2port.item.CompositeArmorItem;
import dev.ic2port.item.HazmatArmorItem;
import dev.ic2port.item.RadioactiveItem;
import dev.ic2port.item.OverclockerUpgradeItem;
import dev.ic2port.item.BrewedPotionItem;
import dev.ic2port.item.BeerItem;
import dev.ic2port.item.PainterItem;
import dev.ic2port.item.ToolboxItem;
import dev.ic2port.item.TransformerUpgradeItem;
import dev.ic2port.item.ReBatteryItem;
import dev.ic2port.item.AdvancedTreeTapItem;
import dev.ic2port.item.ElectricTreeTapItem;
import dev.ic2port.item.ThermometerItem;
import dev.ic2port.item.TreeTapItem;
import dev.ic2port.item.WrenchItem;
import dev.ic2port.setup.BlockRegistry;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Central registry for all {@link Item} instances of this mod.
 */
public final class ItemRegistry {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Reference.MOD_ID);

    public static final RegistryObject<Item> CREATIVE_GENERATOR = ITEMS.register("creative_generator",
            () -> new BlockItem(BlockRegistry.CREATIVE_GENERATOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> HV_CREATIVE_GENERATOR = ITEMS.register("hv_creative_generator",
            () -> new BlockItem(BlockRegistry.HV_CREATIVE_GENERATOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> COPPER_CABLE = ITEMS.register("copper_cable",
            () -> new BlockItem(BlockRegistry.COPPER_CABLE.get(), new Item.Properties()));

    public static final RegistryObject<Item> GOLD_CABLE = ITEMS.register("gold_cable",
            () -> new BlockItem(BlockRegistry.GOLD_CABLE.get(), new Item.Properties()));

    public static final RegistryObject<Item> HV_CABLE = ITEMS.register("hv_cable",
            () -> new BlockItem(BlockRegistry.HV_CABLE.get(), new Item.Properties()));

    public static final RegistryObject<Item> GLASS_FIBER_CABLE = ITEMS.register("glass_fiber_cable",
            () -> new BlockItem(BlockRegistry.GLASS_FIBER_CABLE.get(), new Item.Properties()));

    public static final RegistryObject<Item> MACERATOR = ITEMS.register("macerator",
            () -> new BlockItem(BlockRegistry.MACERATOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> RECYCLER = ITEMS.register("recycler",
            () -> new BlockItem(BlockRegistry.RECYCLER.get(), new Item.Properties()));

    public static final RegistryObject<Item> SOLID_FUEL_GENERATOR = ITEMS.register("solid_fuel_generator",
            () -> new BlockItem(BlockRegistry.SOLID_FUEL_GENERATOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> GEOTHERMAL_GENERATOR = ITEMS.register("geothermal_generator",
            () -> new BlockItem(BlockRegistry.GEOTHERMAL_GENERATOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> SOLAR_PANEL = ITEMS.register("solar_panel",
            () -> new BlockItem(BlockRegistry.SOLAR_PANEL.get(), new Item.Properties()));

    public static final RegistryObject<Item> ADVANCED_SOLAR_PANEL = ITEMS.register("advanced_solar_panel",
            () -> new BlockItem(BlockRegistry.ADVANCED_SOLAR_PANEL.get(), new Item.Properties()));

    public static final RegistryObject<Item> HV_SOLAR_PANEL = ITEMS.register("hv_solar_panel",
            () -> new BlockItem(BlockRegistry.HV_SOLAR_PANEL.get(), new Item.Properties()));

    public static final RegistryObject<Item> DETECTOR_CABLE = ITEMS.register("detector_cable",
            () -> new BlockItem(BlockRegistry.DETECTOR_CABLE.get(), new Item.Properties()));

    public static final RegistryObject<Item> SPLITTER_CABLE = ITEMS.register("splitter_cable",
            () -> new BlockItem(BlockRegistry.SPLITTER_CABLE.get(), new Item.Properties()));

    public static final RegistryObject<Item> TELEPORTER = ITEMS.register("teleporter",
            () -> new BlockItem(BlockRegistry.TELEPORTER.get(), new Item.Properties()));

    public static final RegistryObject<Item> TERRAFORMER = ITEMS.register("terraformer",
            () -> new BlockItem(BlockRegistry.TERRAFORMER.get(), new Item.Properties()));

    public static final RegistryObject<Item> PATTERN_REPLICATOR = ITEMS.register("pattern_replicator",
            () -> new BlockItem(BlockRegistry.PATTERN_REPLICATOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> FREQUENCY_TRANSMITTER = ITEMS.register("frequency_transmitter",
            () -> new dev.ic2port.item.FrequencyTransmitterItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> BLUEPRINT_CULTIVATION = ITEMS.register("blueprint_cultivation",
            () -> new dev.ic2port.item.TerraformerBlueprintItem(
                    dev.ic2port.item.TerraformerBlueprintItem.Mode.CULTIVATION, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> BLUEPRINT_IRRIGATION = ITEMS.register("blueprint_irrigation",
            () -> new dev.ic2port.item.TerraformerBlueprintItem(
                    dev.ic2port.item.TerraformerBlueprintItem.Mode.IRRIGATION, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> BLUEPRINT_DESERTIFICATION = ITEMS.register("blueprint_desertification",
            () -> new dev.ic2port.item.TerraformerBlueprintItem(
                    dev.ic2port.item.TerraformerBlueprintItem.Mode.DESERTIFICATION, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> ELECTRONIC_CIRCUIT = ITEMS.register("electronic_circuit",
            () -> new dev.ic2port.item.CircuitItem(1, new Item.Properties()));

    public static final RegistryObject<Item> ADVANCED_CIRCUIT = ITEMS.register("advanced_circuit",
            () -> new dev.ic2port.item.CircuitItem(2, new Item.Properties()));

    public static final RegistryObject<Item> COMPLEX_CIRCUIT = ITEMS.register("complex_circuit",
            () -> new dev.ic2port.item.CircuitItem(3, new Item.Properties()));

    public static final RegistryObject<Item> ELECTROLYZER = ITEMS.register("electrolyzer",
            () -> new BlockItem(BlockRegistry.ELECTROLYZER.get(), new Item.Properties()));

    public static final RegistryObject<Item> ORE_WASHER = ITEMS.register("ore_washer",
            () -> new BlockItem(BlockRegistry.ORE_WASHER.get(), new Item.Properties()));

    public static final RegistryObject<Item> ALLOY_SMELTER = ITEMS.register("alloy_smelter",
            () -> new BlockItem(BlockRegistry.ALLOY_SMELTER.get(), new Item.Properties()));

    public static final RegistryObject<Item> MINER = ITEMS.register("miner",
            () -> new BlockItem(BlockRegistry.MINER.get(), new Item.Properties()));

    public static final RegistryObject<Item> PUMP = ITEMS.register("pump",
            () -> new BlockItem(BlockRegistry.PUMP.get(), new Item.Properties()));

    public static final RegistryObject<Item> WIND_MILL = ITEMS.register("wind_mill",
            () -> new BlockItem(BlockRegistry.WIND_MILL.get(), new Item.Properties()));

    public static final RegistryObject<Item> WATER_MILL = ITEMS.register("water_mill",
            () -> new BlockItem(BlockRegistry.WATER_MILL.get(), new Item.Properties()));

    public static final RegistryObject<Item> BATBOX = ITEMS.register("batbox",
            () -> new BlockItem(BlockRegistry.BATBOX.get(), new Item.Properties()));

    public static final RegistryObject<Item> MFE = ITEMS.register("mfe",
            () -> new BlockItem(BlockRegistry.MFE.get(), new Item.Properties()));

    public static final RegistryObject<Item> MFSU = ITEMS.register("mfsu",
            () -> new BlockItem(BlockRegistry.MFSU.get(), new Item.Properties()));

    public static final RegistryObject<Item> ESU = ITEMS.register("esu",
            () -> new BlockItem(BlockRegistry.ESU.get(), new Item.Properties()));

    public static final RegistryObject<Item> LV_TRANSFORMER = ITEMS.register("lv_transformer",
            () -> new BlockItem(BlockRegistry.LV_TRANSFORMER.get(), new Item.Properties()));

    public static final RegistryObject<Item> MV_TRANSFORMER = ITEMS.register("mv_transformer",
            () -> new BlockItem(BlockRegistry.MV_TRANSFORMER.get(), new Item.Properties()));

    public static final RegistryObject<Item> EV_TRANSFORMER = ITEMS.register("ev_transformer",
            () -> new BlockItem(BlockRegistry.EV_TRANSFORMER.get(), new Item.Properties()));

    public static final RegistryObject<Item> RE_BATTERY = ITEMS.register("re_battery",
            () -> new ReBatteryItem(new Item.Properties()));

    public static final RegistryObject<Item> BASIC_DRILL = ITEMS.register("basic_drill",
            () -> new BasicDrillItem(new Item.Properties()));

    public static final RegistryObject<Item> CHAINSAW = ITEMS.register("chainsaw",
            () -> new ChainsawItem(new Item.Properties()));

    public static final RegistryObject<Item> ELECTRIC_WRENCH = ITEMS.register("electric_wrench",
            () -> new ElectricWrenchItem(new Item.Properties()));

    public static final RegistryObject<Item> DIAMOND_DRILL = ITEMS.register("diamond_drill",
            () -> new DiamondDrillItem(new Item.Properties()));

    public static final RegistryObject<Item> ADVANCED_DRILL = ITEMS.register("advanced_drill",
            () -> new AdvancedDrillItem(new Item.Properties()));

    public static final RegistryObject<Item> ELECTRIC_JETPACK = ITEMS.register("electric_jetpack",
            () -> new ElectricJetpackItem(new Item.Properties()));

    public static final RegistryObject<Item> BATPACK = ITEMS.register("batpack",
            () -> new BatPackItem(new Item.Properties()));

    public static final RegistryObject<Item> LAPPACK = ITEMS.register("lappack",
            () -> new LapPackItem(new Item.Properties()));

    public static final RegistryObject<Item> ENERGY_CRYSTAL = ITEMS.register("energy_crystal",
            () -> new EnergyCrystalItem(new Item.Properties()));

    public static final RegistryObject<Item> HAZMAT_HELMET = ITEMS.register("hazmat_helmet",
            () -> new HazmatArmorItem(ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> HAZMAT_CHESTPLATE = ITEMS.register("hazmat_chestplate",
            () -> new HazmatArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));

    public static final RegistryObject<Item> HAZMAT_LEGGINGS = ITEMS.register("hazmat_leggings",
            () -> new HazmatArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));

    public static final RegistryObject<Item> HAZMAT_BOOTS = ITEMS.register("hazmat_boots",
            () -> new HazmatArmorItem(ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final RegistryObject<Item> BRONZE_HELMET = ITEMS.register("bronze_helmet",
            () -> new BronzeArmorItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> BRONZE_CHESTPLATE = ITEMS.register("bronze_chestplate",
            () -> new BronzeArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> BRONZE_LEGGINGS = ITEMS.register("bronze_leggings",
            () -> new BronzeArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> BRONZE_BOOTS = ITEMS.register("bronze_boots",
            () -> new BronzeArmorItem(ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final RegistryObject<Item> COMPOSITE_HELMET = ITEMS.register("composite_helmet",
            () -> new CompositeArmorItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> COMPOSITE_CHESTPLATE = ITEMS.register("composite_chestplate",
            () -> new CompositeArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> COMPOSITE_LEGGINGS = ITEMS.register("composite_leggings",
            () -> new CompositeArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> COMPOSITE_BOOTS = ITEMS.register("composite_boots",
            () -> new CompositeArmorItem(ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final RegistryObject<Item> NANO_HELMET = ITEMS.register("nano_helmet",
            () -> new NanoSuitItem(ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> NANO_CHESTPLATE = ITEMS.register("nano_chestplate",
            () -> new NanoSuitItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));

    public static final RegistryObject<Item> NANO_LEGGINGS = ITEMS.register("nano_leggings",
            () -> new NanoSuitItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));

    public static final RegistryObject<Item> NANO_BOOTS = ITEMS.register("nano_boots",
            () -> new NanoSuitItem(ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final RegistryObject<Item> TOOLBOX = ITEMS.register("toolbox",
            () -> new ToolboxItem(new Item.Properties()));

    public static final RegistryObject<Item> OVERCLOCKER_UPGRADE = ITEMS.register("overclocker_upgrade",
            () -> new OverclockerUpgradeItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> TRANSFORMER_UPGRADE = ITEMS.register("transformer_upgrade",
            () -> new TransformerUpgradeItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> ENERGY_STORAGE_UPGRADE = ITEMS.register("energy_storage_upgrade",
            () -> new EnergyStorageUpgradeItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> WRENCH = ITEMS.register("wrench",
            () -> new WrenchItem(new Item.Properties()));

    public static final RegistryObject<Item> TREE_TAP = ITEMS.register("tree_tap",
            () -> new TreeTapItem(new Item.Properties()));

    public static final RegistryObject<Item> ELECTRIC_TREE_TAP = ITEMS.register("electric_tree_tap",
            () -> new ElectricTreeTapItem(new Item.Properties()));

    public static final RegistryObject<Item> ADVANCED_TREE_TAP = ITEMS.register("advanced_tree_tap",
            () -> new AdvancedTreeTapItem(new Item.Properties()));

    public static final RegistryObject<Item> TIN_INGOT = ITEMS.register("tin_ingot",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> RAW_TIN = ITEMS.register("raw_tin",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CRUSHED_IRON_ORE = ITEMS.register("crushed_iron_ore",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> IRON_DUST = ITEMS.register("iron_dust",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CRUSHED_GOLD_ORE = ITEMS.register("crushed_gold_ore",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> GOLD_DUST = ITEMS.register("gold_dust",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CRUSHED_COPPER_ORE = ITEMS.register("crushed_copper_ore",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> COPPER_DUST = ITEMS.register("copper_dust",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CRUSHED_TIN_ORE = ITEMS.register("crushed_tin_ore",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TIN_DUST = ITEMS.register("tin_dust",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TIN_ORE = ITEMS.register("tin_ore",
            () -> new BlockItem(BlockRegistry.TIN_ORE.get(), new Item.Properties()));

    public static final RegistryObject<Item> DEEPSLATE_TIN_ORE = ITEMS.register("deepslate_tin_ore",
            () -> new BlockItem(BlockRegistry.DEEPSLATE_TIN_ORE.get(), new Item.Properties()));

    public static final RegistryObject<Item> URANIUM_ORE = ITEMS.register("uranium_ore",
            () -> new BlockItem(BlockRegistry.URANIUM_ORE.get(), new Item.Properties()));

    public static final RegistryObject<Item> DEEPSLATE_URANIUM_ORE = ITEMS.register("deepslate_uranium_ore",
            () -> new BlockItem(BlockRegistry.DEEPSLATE_URANIUM_ORE.get(), new Item.Properties()));

    public static final RegistryObject<Item> RAW_URANIUM = ITEMS.register("raw_uranium",
            () -> new RadioactiveItem(new Item.Properties()));

    public static final RegistryObject<Item> URANIUM_INGOT = ITEMS.register("uranium_ingot",
            () -> new RadioactiveItem(new Item.Properties()));

    public static final RegistryObject<Item> URANIUM_PLATE = ITEMS.register("uranium_plate",
            () -> new RadioactiveItem(new Item.Properties()));

    public static final RegistryObject<Item> EMPTY_FUEL_ROD = ITEMS.register("empty_fuel_rod",
            () -> new RadioactiveItem(new Item.Properties()));

    public static final RegistryObject<Item> FUEL_ROD = ITEMS.register("fuel_rod",
            () -> new FuelRodItem(new Item.Properties()));

    public static final RegistryObject<Item> DEPLETED_FUEL_ROD = ITEMS.register("depleted_fuel_rod",
            () -> new DepletedFuelRodItem(new Item.Properties()));

    public static final RegistryObject<Item> DEPLETED_URANIUM = ITEMS.register("depleted_uranium",
            () -> new DepletedUraniumItem(new Item.Properties()));

    public static final RegistryObject<Item> PLUTONIUM = ITEMS.register("plutonium",
            () -> new PlutoniumItem(new Item.Properties()));

    public static final RegistryObject<Item> MOX_FUEL_ROD = ITEMS.register("mox_fuel_rod",
            () -> new MoxFuelRodItem(new Item.Properties()));

    public static final RegistryObject<Item> LAPOTRON_CRYSTAL = ITEMS.register("lapotron_crystal",
            () -> new LapotronCrystalItem(new Item.Properties()));

    public static final RegistryObject<Item> SCRAP = ITEMS.register("scrap",
            () -> new ScrapItem(new Item.Properties()));

    public static final RegistryObject<Item> SCRAP_BOX = ITEMS.register("scrap_box",
            () -> new ScrapBoxItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> CENTRIFUGE_ROTOR = ITEMS.register("centrifuge_rotor",
            () -> new CentrifugeRotorItem(new Item.Properties()));

    public static final RegistryObject<Item> OD_SCANNER = ITEMS.register("od_scanner",
            () -> new OdScannerItem(new Item.Properties()));

    public static final RegistryObject<Item> OV_SCANNER = ITEMS.register("ov_scanner",
            () -> new OvScannerItem(new Item.Properties()));

    public static final RegistryObject<Item> MINING_LASER = ITEMS.register("mining_laser",
            () -> new MiningLaserItem(new Item.Properties()));

    public static final RegistryObject<Item> THERMOMETER = ITEMS.register("thermometer",
            () -> new ThermometerItem(new Item.Properties()));

    public static final RegistryObject<Item> EU_READER = ITEMS.register("eu_reader",
            () -> new EuReaderItem(new Item.Properties()));

    public static final RegistryObject<Item> CROP_SEED = ITEMS.register("crop_seed",
            () -> new CropSeedItem(new Item.Properties()));

    public static final RegistryObject<Item> CROP_STICKS = ITEMS.register("crop_sticks",
            () -> new BlockItem(BlockRegistry.CROP_STICKS.get(), new Item.Properties()));

    public static final RegistryObject<Item> FERTILIZER = ITEMS.register("fertilizer",
            () -> new FertilizerItem(new Item.Properties()));

    public static final RegistryObject<Item> CROPNALYZER = ITEMS.register("cropnalyzer",
            () -> new CropnalyzerItem(new Item.Properties()));

    public static final RegistryObject<Item> CROP_HARVESTER = ITEMS.register("crop_harvester",
            () -> new BlockItem(BlockRegistry.CROP_HARVESTER.get(), new Item.Properties()));

    public static final RegistryObject<Item> CROPMATRON = ITEMS.register("cropmatron",
            () -> new BlockItem(BlockRegistry.CROPMATRON.get(), new Item.Properties()));

    public static final RegistryObject<Item> CROP_ANALYZER = ITEMS.register("crop_analyzer",
            () -> new BlockItem(BlockRegistry.CROP_ANALYZER.get(), new Item.Properties()));

    public static final RegistryObject<Item> HOPS = ITEMS.register("hops",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TERRA_WART = ITEMS.register("terra_wart",
            () -> new TerraWartItem(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(2).saturationMod(0.3F).alwaysEat().build())));

    public static final RegistryObject<Item> WET_CONSTRUCTION_FOAM = ITEMS.register("wet_construction_foam",
            () -> new BlockItem(BlockRegistry.WET_CONSTRUCTION_FOAM.get(), new Item.Properties()));

    public static final RegistryObject<Item> CONSTRUCTION_FOAM = ITEMS.register("construction_foam",
            () -> new BlockItem(BlockRegistry.CONSTRUCTION_FOAM.get(), new Item.Properties()));

    public static final RegistryObject<Item> REINFORCED_STONE = ITEMS.register("reinforced_stone",
            () -> new BlockItem(BlockRegistry.REINFORCED_STONE.get(), new Item.Properties()));

    public static final RegistryObject<Item> REINFORCED_GLASS = ITEMS.register("reinforced_glass",
            () -> new BlockItem(BlockRegistry.REINFORCED_GLASS.get(), new Item.Properties()));

    public static final RegistryObject<Item> REINFORCED_DOOR = ITEMS.register("reinforced_door",
            () -> new BlockItem(BlockRegistry.REINFORCED_DOOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> REINFORCED_PLANKS = ITEMS.register("reinforced_planks",
            () -> new BlockItem(BlockRegistry.REINFORCED_PLANKS.get(), new Item.Properties()));

    public static final RegistryObject<Item> REINFORCED_BRICKS = ITEMS.register("reinforced_bricks",
            () -> new BlockItem(BlockRegistry.REINFORCED_BRICKS.get(), new Item.Properties()));

    public static final RegistryObject<Item> REINFORCED_COBBLESTONE = ITEMS.register("reinforced_cobblestone",
            () -> new BlockItem(BlockRegistry.REINFORCED_COBBLESTONE.get(), new Item.Properties()));

    public static final RegistryObject<Item> REINFORCED_CRACKED_STONE = ITEMS.register("reinforced_cracked_stone",
            () -> new BlockItem(BlockRegistry.REINFORCED_CRACKED_STONE.get(), new Item.Properties()));

    public static final RegistryObject<Item> REINFORCED_CLEAR_GLASS = ITEMS.register("reinforced_clear_glass",
            () -> new BlockItem(BlockRegistry.REINFORCED_CLEAR_GLASS.get(), new Item.Properties()));

    public static final RegistryObject<Item> WOODEN_SCAFFOLD = ITEMS.register("wooden_scaffold",
            () -> new BlockItem(BlockRegistry.WOODEN_SCAFFOLD.get(), new Item.Properties()));

    public static final RegistryObject<Item> IRON_SCAFFOLD = ITEMS.register("iron_scaffold",
            () -> new BlockItem(BlockRegistry.IRON_SCAFFOLD.get(), new Item.Properties()));

    public static final RegistryObject<Item> TIN_CAN = ITEMS.register("tin_can",
            () -> new TinCanItem(new Item.Properties()));

    public static final RegistryObject<Item> FILLED_TIN_CAN = ITEMS.register("filled_tin_can",
            () -> new FilledTinCanItem(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(1).saturationMod(0.3F).alwaysEat().build())));

    public static final RegistryObject<Item> AUTO_FEEDER_MODULE = ITEMS.register("auto_feeder_module",
            () -> new AutoFeederModuleItem(new Item.Properties()));

    public static final RegistryObject<Item> FOOD_STORAGE_MODULE = ITEMS.register("food_storage_module",
            () -> new FoodStorageModuleItem(new Item.Properties()));

    public static final RegistryObject<Item> JETPACK_MODULE = ITEMS.register("jetpack_module",
            () -> new JetpackModuleItem(new Item.Properties()));

    public static final RegistryObject<Item> ENERGY_SHIELD_MODULE = ITEMS.register("energy_shield_module",
            () -> new EnergyShieldModuleItem(new Item.Properties()));

    public static final RegistryObject<Item> PORTABLE_ENERGY_PACK_MODULE = ITEMS.register("portable_energy_pack_module",
            () -> new PortableEnergyPackModuleItem(new Item.Properties()));

    public static final RegistryObject<Item> LAPOTRON_ENERGY_PACK_MODULE = ITEMS.register("lapotron_energy_pack_module",
            () -> new LapotronEnergyPackModuleItem(new Item.Properties()));

    public static final RegistryObject<Item> PAINTER = ITEMS.register("painter",
            () -> new PainterItem(new Item.Properties()));

    public static final RegistryObject<Item> BEER = ITEMS.register("beer",
            () -> new BeerItem(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(1).saturationMod(0.1F).alwaysEat().build())));

    public static final RegistryObject<Item> RUM = ITEMS.register("rum",
            () -> new RumItem(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(1).saturationMod(0.1F).alwaysEat().build())));

    public static final RegistryObject<Item> WHISKY = ITEMS.register("whisky",
            () -> new WhiskyItem(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(1).saturationMod(0.1F).alwaysEat().build())));

    public static final RegistryObject<Item> BREWED_POTION = ITEMS.register("brewed_potion",
            () -> new BrewedPotionItem(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(0).alwaysEat().build())));

    public static final RegistryObject<Item> BREWING_BARREL = ITEMS.register("brewing_barrel",
            () -> new BlockItem(BlockRegistry.BREWING_BARREL.get(), new Item.Properties()));

    public static final RegistryObject<Item> HYDRATION_CELL = ITEMS.register("hydration_cell",
            () -> new HydrationCellItem(new Item.Properties()));

    public static final RegistryObject<Item> WEED_EX = ITEMS.register("weed_ex",
            () -> new WeedExItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> FOAM_PELLET = ITEMS.register("foam_pellet",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> FOAM_SPRAYER = ITEMS.register("foam_sprayer",
            () -> new FoamSprayerItem(new Item.Properties()));

    public static final RegistryObject<Item> ELECTRIC_FOAM_SPRAYER = ITEMS.register("electric_foam_sprayer",
            () -> new ElectricFoamSprayerItem(new Item.Properties()));

    public static final RegistryObject<Item> OBSCURATOR = ITEMS.register("obscurator",
            () -> new ObscuratorItem(new Item.Properties()));

    public static final RegistryObject<Item> UU_MATTER = ITEMS.register("uu_matter",
            () -> new UuMatterItem(new Item.Properties()));

    public static final RegistryObject<Item> IRIDIUM = ITEMS.register("iridium",
            () -> new IridiumItem(new Item.Properties()));

    public static final RegistryObject<Item> IRIDIUM_PLATE = ITEMS.register("iridium_plate",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> QUANTUM_HELMET = ITEMS.register("quantum_helmet",
            () -> new QuantumSuitItem(ArmorItem.Type.HELMET, new Item.Properties(), QuantumSuitItem.getCapacityFor(ArmorItem.Type.HELMET)));

    public static final RegistryObject<Item> QUANTUM_CHESTPLATE = ITEMS.register("quantum_chestplate",
            () -> new QuantumSuitItem(ArmorItem.Type.CHESTPLATE, new Item.Properties(), QuantumSuitItem.getCapacityFor(ArmorItem.Type.CHESTPLATE)));

    public static final RegistryObject<Item> QUANTUM_LEGGINGS = ITEMS.register("quantum_leggings",
            () -> new QuantumSuitItem(ArmorItem.Type.LEGGINGS, new Item.Properties(), QuantumSuitItem.getCapacityFor(ArmorItem.Type.LEGGINGS)));

    public static final RegistryObject<Item> QUANTUM_BOOTS = ITEMS.register("quantum_boots",
            () -> new QuantumSuitItem(ArmorItem.Type.BOOTS, new Item.Properties(), QuantumSuitItem.getCapacityFor(ArmorItem.Type.BOOTS)));

    public static final RegistryObject<Item> NUCLEAR_REACTOR = ITEMS.register("nuclear_reactor",
            () -> new BlockItem(BlockRegistry.NUCLEAR_REACTOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> FUSION_REACTOR = ITEMS.register("fusion_reactor",
            () -> new BlockItem(BlockRegistry.FUSION_REACTOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> FUSION_REACTOR_VALVE = ITEMS.register("fusion_reactor_valve",
            () -> new BlockItem(BlockRegistry.FUSION_REACTOR_VALVE.get(), new Item.Properties()));

    public static final RegistryObject<Item> REACTOR_CHAMBER = ITEMS.register("reactor_chamber",
            () -> new BlockItem(BlockRegistry.REACTOR_CHAMBER.get(), new Item.Properties()));

    public static final RegistryObject<Item> CONTAMINATED_SOIL = ITEMS.register("contaminated_soil",
            () -> new BlockItem(BlockRegistry.CONTAMINATED_SOIL.get(), new Item.Properties()));

    public static final RegistryObject<Item> HEAT_VENT = ITEMS.register("heat_vent",
            () -> new HeatVentItem(new Item.Properties(), 12.0D, 1_000.0D, 60.0D));

    public static final RegistryObject<Item> ADVANCED_HEAT_VENT = ITEMS.register("advanced_heat_vent",
            () -> new HeatVentItem(new Item.Properties(), 24.0D, 2_000.0D, 120.0D));

    public static final RegistryObject<Item> OVERCLOCKED_HEAT_VENT = ITEMS.register("overclocked_heat_vent",
            () -> new HeatVentItem(new Item.Properties(), 36.0D, 2_500.0D, 180.0D));

    public static final RegistryObject<Item> HEAT_EXCHANGER = ITEMS.register("heat_exchanger",
            () -> new HeatExchangerItem(new Item.Properties(), 4.0D, 2_500.0D));

    public static final RegistryObject<Item> ADVANCED_HEAT_EXCHANGER = ITEMS.register("advanced_heat_exchanger",
            () -> new HeatExchangerItem(new Item.Properties(), 8.0D, 5_000.0D));

    public static final RegistryObject<Item> COOLANT_CELL = ITEMS.register("coolant_cell",
            () -> new CoolantCellItem(new Item.Properties(), 10_000.0D, 100.0D));

    public static final RegistryObject<Item> TRIPLE_COOLANT_CELL = ITEMS.register("triple_coolant_cell",
            () -> new CoolantCellItem(new Item.Properties(), 30_000.0D, 200.0D));

    public static final RegistryObject<Item> QUAD_COOLANT_CELL = ITEMS.register("quad_coolant_cell",
            () -> new CoolantCellItem(new Item.Properties(), 60_000.0D, 300.0D));

    public static final RegistryObject<Item> NEUTRON_REFLECTOR = ITEMS.register("neutron_reflector",
            () -> new NeutronReflectorItem(new Item.Properties(), false));

    public static final RegistryObject<Item> THICK_NEUTRON_REFLECTOR = ITEMS.register("thick_neutron_reflector",
            () -> new NeutronReflectorItem(new Item.Properties(), true));

    public static final RegistryObject<Item> REACTOR_PLATING = ITEMS.register("reactor_plating",
            () -> new ReactorPlatingItem(new Item.Properties()));

    public static final RegistryObject<Item> RSH_CONDENSATOR = ITEMS.register("rsh_condensator",
            () -> new ReactorCondensatorItem(new Item.Properties(), 10_000.0D, 20.0D));

    public static final RegistryObject<Item> LZH_CONDENSATOR = ITEMS.register("lzh_condensator",
            () -> new ReactorCondensatorItem(new Item.Properties(), 100_000.0D, 100.0D));

    public static final RegistryObject<Item> DUAL_FUEL_ROD = ITEMS.register("dual_fuel_rod",
            () -> new DualFuelRodItem(new Item.Properties()));

    public static final RegistryObject<Item> QUAD_FUEL_ROD = ITEMS.register("quad_fuel_rod",
            () -> new QuadFuelRodItem(new Item.Properties()));

    public static final RegistryObject<Item> FLUID_CELL = ITEMS.register("fluid_cell",
            () -> new FluidCellItem(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> STICKY_RESIN = ITEMS.register("sticky_resin",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> RUBBER = ITEMS.register("rubber",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> EXTRACTOR = ITEMS.register("extractor",
            () -> new BlockItem(BlockRegistry.EXTRACTOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> CENTRIFUGAL_EXTRACTOR = ITEMS.register("centrifugal_extractor",
            () -> new BlockItem(BlockRegistry.CENTRIFUGAL_EXTRACTOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> RUBBER_WOOD = ITEMS.register("rubber_wood",
            () -> new BlockItem(BlockRegistry.RUBBER_WOOD.get(), new Item.Properties()));

    public static final RegistryObject<Item> RUBBER_SAPLING = ITEMS.register("rubber_sapling",
            () -> new BlockItem(BlockRegistry.RUBBER_SAPLING.get(), new Item.Properties()));

    public static final RegistryObject<Item> RUBBER_LEAVES = ITEMS.register("rubber_leaves",
            () -> new BlockItem(BlockRegistry.RUBBER_LEAVES.get(), new Item.Properties()));

    public static final RegistryObject<Item> COMPRESSOR = ITEMS.register("compressor",
            () -> new BlockItem(BlockRegistry.COMPRESSOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> CANNER = ITEMS.register("canner",
            () -> new BlockItem(BlockRegistry.CANNER.get(), new Item.Properties()));

    public static final RegistryObject<Item> VACUUM_CANNER = ITEMS.register("vacuum_canner",
            () -> new BlockItem(BlockRegistry.VACUUM_CANNER.get(), new Item.Properties()));

    public static final RegistryObject<Item> ELECTRIC_FURNACE = ITEMS.register("electric_furnace",
            () -> new BlockItem(BlockRegistry.ELECTRIC_FURNACE.get(), new Item.Properties()));

    public static final RegistryObject<Item> INDUCTION_FURNACE = ITEMS.register("induction_furnace",
            () -> new BlockItem(BlockRegistry.INDUCTION_FURNACE.get(), new Item.Properties()));

    public static final RegistryObject<Item> BLAST_INDUCTION_FURNACE = ITEMS.register("blast_induction_furnace",
            () -> new BlockItem(BlockRegistry.BLAST_INDUCTION_FURNACE.get(), new Item.Properties()));

    public static final RegistryObject<Item> METAL_FORMER = ITEMS.register("metal_former",
            () -> new BlockItem(BlockRegistry.METAL_FORMER.get(), new Item.Properties()));

    public static final RegistryObject<Item> CHARGE_PAD = ITEMS.register("charge_pad",
            () -> new ChargePadItem(BlockRegistry.CHARGE_PAD.get(), new Item.Properties()));

    public static final RegistryObject<Item> THERMAL_CENTRIFUGE = ITEMS.register("thermal_centrifuge",
            () -> new BlockItem(BlockRegistry.THERMAL_CENTRIFUGE.get(), new Item.Properties()));

    public static final RegistryObject<Item> MASS_FABRICATOR = ITEMS.register("mass_fabricator",
            () -> new BlockItem(BlockRegistry.MASS_FABRICATOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> BRONZE_INGOT = ITEMS.register("bronze_ingot",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> IRON_PLATE = ITEMS.register("iron_plate",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> COPPER_PLATE = ITEMS.register("copper_plate",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> TIN_PLATE = ITEMS.register("tin_plate",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BRONZE_PLATE = ITEMS.register("bronze_plate",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> MIXED_METAL_INGOT = ITEMS.register("mixed_metal_ingot",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ADVANCED_ALLOY = ITEMS.register("advanced_alloy",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CARBON_FIBER = ITEMS.register("carbon_fiber",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> RAW_CARBON_MESH = ITEMS.register("raw_carbon_mesh",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CARBON_PLATE = ITEMS.register("carbon_plate",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BASIC_MACHINE_CASING = ITEMS.register("basic_machine_casing",
            () -> new BlockItem(BlockRegistry.BASIC_MACHINE_CASING.get(), new Item.Properties()));

    public static final RegistryObject<Item> ADVANCED_MACHINE_CASING = ITEMS.register("advanced_machine_casing",
            () -> new BlockItem(BlockRegistry.ADVANCED_MACHINE_CASING.get(), new Item.Properties()));

    private ItemRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void register(final IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
