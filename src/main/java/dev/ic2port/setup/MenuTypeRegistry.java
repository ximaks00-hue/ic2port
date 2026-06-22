package dev.ic2port.setup;

import dev.ic2port.Reference;
import dev.ic2port.menu.AlloySmelterMenu;
import dev.ic2port.menu.ArmorModulesMenu;
import dev.ic2port.menu.ElectrolyzerMenu;
import dev.ic2port.menu.OreWasherMenu;
import dev.ic2port.menu.PatternReplicatorMenu;
import dev.ic2port.menu.AutoFeederMenu;
import dev.ic2port.menu.BrewingBarrelMenu;
import dev.ic2port.menu.FoodStorageModuleMenu;
import dev.ic2port.menu.CropAnalyzerMenu;
import dev.ic2port.menu.CropHarvesterMenu;
import dev.ic2port.menu.CropmatronMenu;
import dev.ic2port.menu.FusionReactorMenu;
import dev.ic2port.menu.VacuumCannerMenu;
import dev.ic2port.menu.BatBoxMenu;
import dev.ic2port.menu.CannerMenu;
import dev.ic2port.menu.CompressorMenu;
import dev.ic2port.menu.ElectricFurnaceMenu;
import dev.ic2port.menu.ExtractorMenu;
import dev.ic2port.menu.InductionFurnaceMenu;
import dev.ic2port.menu.MetalFormerMenu;
import dev.ic2port.menu.EsuMenu;
import dev.ic2port.menu.MFEMenu;
import dev.ic2port.menu.MFSUMenu;
import dev.ic2port.menu.MaceratorMenu;
import dev.ic2port.menu.RecyclerMenu;
import dev.ic2port.menu.NuclearReactorMenu;
import dev.ic2port.menu.GeothermalGeneratorMenu;
import dev.ic2port.menu.ToolboxMenu;
import dev.ic2port.menu.MassFabricatorMenu;
import dev.ic2port.menu.ThermalCentrifugeMenu;
import dev.ic2port.menu.SolidFuelGeneratorMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Central registry for all {@link MenuType} instances of this mod.
 */
public final class MenuTypeRegistry {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Reference.MOD_ID);
    public static final RegistryObject<MenuType<MaceratorMenu>> MACERATOR_MENU =
            MENUS.register("macerator", () -> IForgeMenuType.create(MaceratorMenu::new));
    public static final RegistryObject<MenuType<RecyclerMenu>> RECYCLER_MENU =
            MENUS.register("recycler", () -> IForgeMenuType.create(RecyclerMenu::new));
    public static final RegistryObject<MenuType<ExtractorMenu>> EXTRACTOR_MENU =
            MENUS.register("extractor", () -> IForgeMenuType.create(ExtractorMenu::new));
    public static final RegistryObject<MenuType<CompressorMenu>> COMPRESSOR_MENU =
            MENUS.register("compressor", () -> IForgeMenuType.create(CompressorMenu::new));
    public static final RegistryObject<MenuType<CannerMenu>> CANNER_MENU =
            MENUS.register("canner", () -> IForgeMenuType.create(CannerMenu::new));
    public static final RegistryObject<MenuType<VacuumCannerMenu>> VACUUM_CANNER_MENU =
            MENUS.register("vacuum_canner", () -> IForgeMenuType.create(VacuumCannerMenu::new));
    public static final RegistryObject<MenuType<ElectricFurnaceMenu>> ELECTRIC_FURNACE_MENU =
            MENUS.register("electric_furnace", () -> IForgeMenuType.create(ElectricFurnaceMenu::new));
    public static final RegistryObject<MenuType<InductionFurnaceMenu>> INDUCTION_FURNACE_MENU =
            MENUS.register("induction_furnace", () -> IForgeMenuType.create(InductionFurnaceMenu::new));
    public static final RegistryObject<MenuType<MetalFormerMenu>> METAL_FORMER_MENU =
            MENUS.register("metal_former", () -> IForgeMenuType.create(MetalFormerMenu::new));
    public static final RegistryObject<MenuType<SolidFuelGeneratorMenu>> GENERATOR_MENU =
            MENUS.register("generator", () -> IForgeMenuType.create(SolidFuelGeneratorMenu::new));
    public static final RegistryObject<MenuType<GeothermalGeneratorMenu>> GEOTHERMAL_GENERATOR_MENU =
            MENUS.register("geothermal_generator", () -> IForgeMenuType.create(GeothermalGeneratorMenu::new));
    public static final RegistryObject<MenuType<ToolboxMenu>> TOOLBOX_MENU =
            MENUS.register("toolbox", () -> IForgeMenuType.create(ToolboxMenu::new));
    public static final RegistryObject<MenuType<BatBoxMenu>> BATBOX_MENU =
            MENUS.register("batbox", () -> IForgeMenuType.create(BatBoxMenu::new));
    public static final RegistryObject<MenuType<MFEMenu>> MFE_MENU =
            MENUS.register("mfe", () -> IForgeMenuType.create(MFEMenu::new));
    public static final RegistryObject<MenuType<MFSUMenu>> MFSU_MENU =
            MENUS.register("mfsu", () -> IForgeMenuType.create(MFSUMenu::new));
    public static final RegistryObject<MenuType<EsuMenu>> ESU_MENU =
            MENUS.register("esu", () -> IForgeMenuType.create(EsuMenu::new));
    public static final RegistryObject<MenuType<NuclearReactorMenu>> NUCLEAR_REACTOR_MENU =
            MENUS.register("nuclear_reactor", () -> IForgeMenuType.create(NuclearReactorMenu::new));
    public static final RegistryObject<MenuType<ThermalCentrifugeMenu>> THERMAL_CENTRIFUGE_MENU =
            MENUS.register("thermal_centrifuge", () -> IForgeMenuType.create(ThermalCentrifugeMenu::new));
    public static final RegistryObject<MenuType<MassFabricatorMenu>> MASS_FABRICATOR_MENU =
            MENUS.register("mass_fabricator", () -> IForgeMenuType.create(MassFabricatorMenu::new));
    public static final RegistryObject<MenuType<BrewingBarrelMenu>> BREWING_BARREL_MENU =
            MENUS.register("brewing_barrel", () -> IForgeMenuType.create(BrewingBarrelMenu::new));
    public static final RegistryObject<MenuType<AutoFeederMenu>> AUTO_FEEDER_MENU =
            MENUS.register("auto_feeder", () -> IForgeMenuType.create(AutoFeederMenu::new));
    public static final RegistryObject<MenuType<FoodStorageModuleMenu>> FOOD_STORAGE_MODULE_MENU =
            MENUS.register("food_storage_module", () -> IForgeMenuType.create(FoodStorageModuleMenu::new));
    public static final RegistryObject<MenuType<ArmorModulesMenu>> ARMOR_MODULES_MENU =
            MENUS.register("armor_modules", () -> IForgeMenuType.create(ArmorModulesMenu::new));
    public static final RegistryObject<MenuType<FusionReactorMenu>> FUSION_REACTOR_MENU =
            MENUS.register("fusion_reactor", () -> IForgeMenuType.create(FusionReactorMenu::new));
    public static final RegistryObject<MenuType<CropmatronMenu>> CROPMATRON_MENU =
            MENUS.register("cropmatron", () -> IForgeMenuType.create(CropmatronMenu::new));
    public static final RegistryObject<MenuType<CropHarvesterMenu>> CROP_HARVESTER_MENU =
            MENUS.register("crop_harvester", () -> IForgeMenuType.create(CropHarvesterMenu::new));
    public static final RegistryObject<MenuType<CropAnalyzerMenu>> CROP_ANALYZER_MENU =
            MENUS.register("crop_analyzer", () -> IForgeMenuType.create(CropAnalyzerMenu::new));

    public static final RegistryObject<MenuType<ElectrolyzerMenu>> ELECTROLYZER_MENU =
            MENUS.register("electrolyzer", () -> IForgeMenuType.create(ElectrolyzerMenu::new));

    public static final RegistryObject<MenuType<OreWasherMenu>> ORE_WASHER_MENU =
            MENUS.register("ore_washer", () -> IForgeMenuType.create(OreWasherMenu::new));

    public static final RegistryObject<MenuType<AlloySmelterMenu>> ALLOY_SMELTER_MENU =
            MENUS.register("alloy_smelter", () -> IForgeMenuType.create(AlloySmelterMenu::new));

    public static final RegistryObject<MenuType<PatternReplicatorMenu>> PATTERN_REPLICATOR_MENU =
            MENUS.register("pattern_replicator", () -> IForgeMenuType.create(PatternReplicatorMenu::new));

    private MenuTypeRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void register(final IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
