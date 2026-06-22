package dev.ic2port.client;

import dev.ic2port.Reference;
import dev.ic2port.block.ConstructionFoamBlock;
import dev.ic2port.client.screen.AlloySmelterScreen;
import dev.ic2port.client.screen.PatternReplicatorScreen;
import dev.ic2port.client.screen.ArmorModulesScreen;
import dev.ic2port.client.screen.ElectrolyzerScreen;
import dev.ic2port.client.screen.OreWasherScreen;
import dev.ic2port.client.screen.FoodStorageModuleScreen;
import dev.ic2port.client.screen.FusionReactorScreen;
import dev.ic2port.client.screen.AutoFeederScreen;
import dev.ic2port.client.screen.BrewingBarrelScreen;
import dev.ic2port.client.screen.BatBoxScreen;
import dev.ic2port.client.screen.CannerScreen;
import dev.ic2port.client.screen.CropAnalyzerScreen;
import dev.ic2port.client.screen.CropHarvesterScreen;
import dev.ic2port.client.screen.CropmatronScreen;
import dev.ic2port.client.screen.CompressorScreen;
import dev.ic2port.client.screen.ElectricFurnaceScreen;
import dev.ic2port.client.screen.ExtractorScreen;
import dev.ic2port.client.screen.GeothermalGeneratorScreen;
import dev.ic2port.client.screen.InductionFurnaceScreen;
import dev.ic2port.client.screen.EsuScreen;
import dev.ic2port.client.screen.MFEScreen;
import dev.ic2port.client.screen.MFSUScreen;
import dev.ic2port.client.screen.MaceratorScreen;
import dev.ic2port.client.screen.MassFabricatorScreen;
import dev.ic2port.client.screen.MetalFormerScreen;
import dev.ic2port.client.screen.NuclearReactorScreen;
import dev.ic2port.client.screen.RecyclerScreen;
import dev.ic2port.client.screen.SolidFuelGeneratorScreen;
import dev.ic2port.client.screen.VacuumCannerScreen;
import dev.ic2port.client.screen.ThermalCentrifugeScreen;
import dev.ic2port.client.screen.ToolboxScreen;
import dev.ic2port.setup.BlockRegistry;
import dev.ic2port.setup.MenuTypeRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import dev.ic2port.client.render.ConstructionFoamRenderer;
import dev.ic2port.setup.BlockEntityRegistry;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client-only event hooks.
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    private ClientModEvents() {
        throw new UnsupportedOperationException("Utility class");
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(MenuTypeRegistry.MACERATOR_MENU.get(), MaceratorScreen::new);
            MenuScreens.register(MenuTypeRegistry.RECYCLER_MENU.get(), RecyclerScreen::new);
            MenuScreens.register(MenuTypeRegistry.EXTRACTOR_MENU.get(), ExtractorScreen::new);
            MenuScreens.register(MenuTypeRegistry.COMPRESSOR_MENU.get(), CompressorScreen::new);
            MenuScreens.register(MenuTypeRegistry.CANNER_MENU.get(), CannerScreen::new);
            MenuScreens.register(MenuTypeRegistry.VACUUM_CANNER_MENU.get(), VacuumCannerScreen::new);
            MenuScreens.register(MenuTypeRegistry.ELECTRIC_FURNACE_MENU.get(), ElectricFurnaceScreen::new);
            MenuScreens.register(MenuTypeRegistry.INDUCTION_FURNACE_MENU.get(), InductionFurnaceScreen::new);
            MenuScreens.register(MenuTypeRegistry.METAL_FORMER_MENU.get(), MetalFormerScreen::new);
            MenuScreens.register(MenuTypeRegistry.GENERATOR_MENU.get(), SolidFuelGeneratorScreen::new);
            MenuScreens.register(MenuTypeRegistry.GEOTHERMAL_GENERATOR_MENU.get(), GeothermalGeneratorScreen::new);
            MenuScreens.register(MenuTypeRegistry.TOOLBOX_MENU.get(), ToolboxScreen::new);
            MenuScreens.register(MenuTypeRegistry.BATBOX_MENU.get(), BatBoxScreen::new);
            MenuScreens.register(MenuTypeRegistry.MFE_MENU.get(), MFEScreen::new);
            MenuScreens.register(MenuTypeRegistry.MFSU_MENU.get(), MFSUScreen::new);
            MenuScreens.register(MenuTypeRegistry.ESU_MENU.get(), EsuScreen::new);
            MenuScreens.register(MenuTypeRegistry.NUCLEAR_REACTOR_MENU.get(), NuclearReactorScreen::new);
            MenuScreens.register(MenuTypeRegistry.THERMAL_CENTRIFUGE_MENU.get(), ThermalCentrifugeScreen::new);
            MenuScreens.register(MenuTypeRegistry.MASS_FABRICATOR_MENU.get(), MassFabricatorScreen::new);
            MenuScreens.register(MenuTypeRegistry.BREWING_BARREL_MENU.get(), BrewingBarrelScreen::new);
            MenuScreens.register(MenuTypeRegistry.AUTO_FEEDER_MENU.get(), AutoFeederScreen::new);
            MenuScreens.register(MenuTypeRegistry.FOOD_STORAGE_MODULE_MENU.get(), FoodStorageModuleScreen::new);
            MenuScreens.register(MenuTypeRegistry.ARMOR_MODULES_MENU.get(), ArmorModulesScreen::new);
            MenuScreens.register(MenuTypeRegistry.FUSION_REACTOR_MENU.get(), FusionReactorScreen::new);
            MenuScreens.register(MenuTypeRegistry.CROPMATRON_MENU.get(), CropmatronScreen::new);
            MenuScreens.register(MenuTypeRegistry.CROP_HARVESTER_MENU.get(), CropHarvesterScreen::new);
            MenuScreens.register(MenuTypeRegistry.CROP_ANALYZER_MENU.get(), CropAnalyzerScreen::new);
            MenuScreens.register(MenuTypeRegistry.ELECTROLYZER_MENU.get(), ElectrolyzerScreen::new);
            MenuScreens.register(MenuTypeRegistry.ORE_WASHER_MENU.get(), OreWasherScreen::new);
            MenuScreens.register(MenuTypeRegistry.ALLOY_SMELTER_MENU.get(), AlloySmelterScreen::new);
            MenuScreens.register(MenuTypeRegistry.PATTERN_REPLICATOR_MENU.get(), PatternReplicatorScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityRegistry.CONSTRUCTION_FOAM_BE.get(), ConstructionFoamRenderer::new);
    }

    @SubscribeEvent
    public static void registerBlockColors(final RegisterColorHandlersEvent.Block event) {
        event.register(
                (state, level, pos, tintIndex) -> {
                    DyeColor color = state.getValue(ConstructionFoamBlock.COLOR);
                    return color.getFireworkColor() | 0xFF000000;
                },
                BlockRegistry.CONSTRUCTION_FOAM.get());
    }
}
