package dev.ic2port.setup;

import dev.ic2port.Reference;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Creative mode tab registration for mod content.
 * <p>
 * Uses a vanilla placeholder icon until mod items are registered.
 */
public final class CreativeTabRegistry {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Reference.MOD_ID);

    public static final RegistryObject<CreativeModeTab> IC2_TAB = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + Reference.MOD_ID))
                    .icon(() -> new ItemStack(ItemRegistry.UU_MATTER.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ItemRegistry.CREATIVE_GENERATOR.get());
                        output.accept(ItemRegistry.HV_CREATIVE_GENERATOR.get());
                        output.accept(ItemRegistry.COPPER_CABLE.get());
                        output.accept(ItemRegistry.GOLD_CABLE.get());
                        output.accept(ItemRegistry.HV_CABLE.get());
                        output.accept(ItemRegistry.GLASS_FIBER_CABLE.get());
                        output.accept(ItemRegistry.MACERATOR.get());
                        output.accept(ItemRegistry.RECYCLER.get());
                        output.accept(ItemRegistry.EXTRACTOR.get());
                        output.accept(ItemRegistry.CENTRIFUGAL_EXTRACTOR.get());
                        output.accept(ItemRegistry.COMPRESSOR.get());
                        output.accept(ItemRegistry.CANNER.get());
                        output.accept(ItemRegistry.VACUUM_CANNER.get());
                        output.accept(ItemRegistry.ELECTRIC_FURNACE.get());
                        output.accept(ItemRegistry.INDUCTION_FURNACE.get());
                        output.accept(ItemRegistry.BLAST_INDUCTION_FURNACE.get());
                        output.accept(ItemRegistry.METAL_FORMER.get());
                        output.accept(ItemRegistry.CHARGE_PAD.get());
                        output.accept(ItemRegistry.SOLID_FUEL_GENERATOR.get());
                        output.accept(ItemRegistry.GEOTHERMAL_GENERATOR.get());
                        output.accept(ItemRegistry.SOLAR_PANEL.get());
                        output.accept(ItemRegistry.ADVANCED_SOLAR_PANEL.get());
                        output.accept(ItemRegistry.HV_SOLAR_PANEL.get());
                        output.accept(ItemRegistry.DETECTOR_CABLE.get());
                        output.accept(ItemRegistry.SPLITTER_CABLE.get());
                        output.accept(ItemRegistry.TELEPORTER.get());
                        output.accept(ItemRegistry.TERRAFORMER.get());
                        output.accept(ItemRegistry.PATTERN_REPLICATOR.get());
                        output.accept(ItemRegistry.FREQUENCY_TRANSMITTER.get());
                        output.accept(ItemRegistry.BLUEPRINT_CULTIVATION.get());
                        output.accept(ItemRegistry.BLUEPRINT_IRRIGATION.get());
                        output.accept(ItemRegistry.BLUEPRINT_DESERTIFICATION.get());
                        output.accept(ItemRegistry.ELECTRONIC_CIRCUIT.get());
                        output.accept(ItemRegistry.ADVANCED_CIRCUIT.get());
                        output.accept(ItemRegistry.COMPLEX_CIRCUIT.get());
                        output.accept(ItemRegistry.ELECTROLYZER.get());
                        output.accept(ItemRegistry.ORE_WASHER.get());
                        output.accept(ItemRegistry.ALLOY_SMELTER.get());
                        output.accept(ItemRegistry.MINER.get());
                        output.accept(ItemRegistry.PUMP.get());
                        output.accept(ItemRegistry.WIND_MILL.get());
                        output.accept(ItemRegistry.WATER_MILL.get());
                        output.accept(ItemRegistry.BATBOX.get());
                        output.accept(ItemRegistry.MFE.get());
                        output.accept(ItemRegistry.MFSU.get());
                        output.accept(ItemRegistry.ESU.get());
                        output.accept(ItemRegistry.LV_TRANSFORMER.get());
                        output.accept(ItemRegistry.MV_TRANSFORMER.get());
                        output.accept(ItemRegistry.EV_TRANSFORMER.get());
                        output.accept(ItemRegistry.RE_BATTERY.get());
                        output.accept(ItemRegistry.BASIC_DRILL.get());
                        output.accept(ItemRegistry.CHAINSAW.get());
                        output.accept(ItemRegistry.ELECTRIC_WRENCH.get());
                        output.accept(ItemRegistry.DIAMOND_DRILL.get());
                        output.accept(ItemRegistry.ADVANCED_DRILL.get());
                        output.accept(ItemRegistry.ELECTRIC_JETPACK.get());
                        output.accept(ItemRegistry.BATPACK.get());
                        output.accept(ItemRegistry.LAPPACK.get());
                        output.accept(ItemRegistry.ENERGY_CRYSTAL.get());
                        output.accept(ItemRegistry.TOOLBOX.get());
                        output.accept(ItemRegistry.OVERCLOCKER_UPGRADE.get());
                        output.accept(ItemRegistry.TRANSFORMER_UPGRADE.get());
                        output.accept(ItemRegistry.ENERGY_STORAGE_UPGRADE.get());
                        output.accept(ItemRegistry.WRENCH.get());
                        output.accept(ItemRegistry.TREE_TAP.get());
                        output.accept(ItemRegistry.ELECTRIC_TREE_TAP.get());
                        output.accept(ItemRegistry.ADVANCED_TREE_TAP.get());
                        output.accept(ItemRegistry.TIN_ORE.get());
                        output.accept(ItemRegistry.DEEPSLATE_TIN_ORE.get());
                        output.accept(ItemRegistry.URANIUM_ORE.get());
                        output.accept(ItemRegistry.DEEPSLATE_URANIUM_ORE.get());
                        output.accept(ItemRegistry.RAW_URANIUM.get());
                        output.accept(ItemRegistry.URANIUM_INGOT.get());
                        output.accept(ItemRegistry.URANIUM_PLATE.get());
                        output.accept(ItemRegistry.EMPTY_FUEL_ROD.get());
                        output.accept(ItemRegistry.FUEL_ROD.get());
                        output.accept(ItemRegistry.DEPLETED_FUEL_ROD.get());
                        output.accept(ItemRegistry.DEPLETED_URANIUM.get());
                        output.accept(ItemRegistry.PLUTONIUM.get());
                        output.accept(ItemRegistry.MOX_FUEL_ROD.get());
                        output.accept(ItemRegistry.THERMAL_CENTRIFUGE.get());
                        output.accept(ItemRegistry.CENTRIFUGE_ROTOR.get());
                        output.accept(ItemRegistry.OD_SCANNER.get());
                        output.accept(ItemRegistry.OV_SCANNER.get());
                        output.accept(ItemRegistry.MINING_LASER.get());
                        output.accept(ItemRegistry.THERMOMETER.get());
                        output.accept(ItemRegistry.EU_READER.get());
                        output.accept(ItemRegistry.LAPOTRON_CRYSTAL.get());
                        output.accept(ItemRegistry.SCRAP.get());
                        output.accept(ItemRegistry.SCRAP_BOX.get());
                        output.accept(ItemRegistry.UU_MATTER.get());
                        output.accept(ItemRegistry.IRIDIUM.get());
                        output.accept(ItemRegistry.IRIDIUM_PLATE.get());
                        output.accept(ItemRegistry.QUANTUM_HELMET.get());
                        output.accept(ItemRegistry.QUANTUM_CHESTPLATE.get());
                        output.accept(ItemRegistry.QUANTUM_LEGGINGS.get());
                        output.accept(ItemRegistry.QUANTUM_BOOTS.get());
                        output.accept(ItemRegistry.MASS_FABRICATOR.get());
                        output.accept(ItemRegistry.NUCLEAR_REACTOR.get());
                        output.accept(ItemRegistry.FUSION_REACTOR.get());
                        output.accept(ItemRegistry.FUSION_REACTOR_VALVE.get());
                        output.accept(ItemRegistry.REACTOR_CHAMBER.get());
                        output.accept(ItemRegistry.CONTAMINATED_SOIL.get());
                        output.accept(ItemRegistry.HEAT_VENT.get());
                        output.accept(ItemRegistry.ADVANCED_HEAT_VENT.get());
                        output.accept(ItemRegistry.OVERCLOCKED_HEAT_VENT.get());
                        output.accept(ItemRegistry.HEAT_EXCHANGER.get());
                        output.accept(ItemRegistry.ADVANCED_HEAT_EXCHANGER.get());
                        output.accept(ItemRegistry.COOLANT_CELL.get());
                        output.accept(ItemRegistry.TRIPLE_COOLANT_CELL.get());
                        output.accept(ItemRegistry.QUAD_COOLANT_CELL.get());
                        output.accept(ItemRegistry.NEUTRON_REFLECTOR.get());
                        output.accept(ItemRegistry.THICK_NEUTRON_REFLECTOR.get());
                        output.accept(ItemRegistry.REACTOR_PLATING.get());
                        output.accept(ItemRegistry.RSH_CONDENSATOR.get());
                        output.accept(ItemRegistry.LZH_CONDENSATOR.get());
                        output.accept(ItemRegistry.DUAL_FUEL_ROD.get());
                        output.accept(ItemRegistry.QUAD_FUEL_ROD.get());
                        output.accept(ItemRegistry.HAZMAT_HELMET.get());
                        output.accept(ItemRegistry.HAZMAT_CHESTPLATE.get());
                        output.accept(ItemRegistry.HAZMAT_LEGGINGS.get());
                        output.accept(ItemRegistry.HAZMAT_BOOTS.get());
                        output.accept(ItemRegistry.BRONZE_HELMET.get());
                        output.accept(ItemRegistry.BRONZE_CHESTPLATE.get());
                        output.accept(ItemRegistry.BRONZE_LEGGINGS.get());
                        output.accept(ItemRegistry.BRONZE_BOOTS.get());
                        output.accept(ItemRegistry.COMPOSITE_HELMET.get());
                        output.accept(ItemRegistry.COMPOSITE_CHESTPLATE.get());
                        output.accept(ItemRegistry.COMPOSITE_LEGGINGS.get());
                        output.accept(ItemRegistry.COMPOSITE_BOOTS.get());
                        output.accept(ItemRegistry.NANO_HELMET.get());
                        output.accept(ItemRegistry.NANO_CHESTPLATE.get());
                        output.accept(ItemRegistry.NANO_LEGGINGS.get());
                        output.accept(ItemRegistry.NANO_BOOTS.get());
                        output.accept(ItemRegistry.RAW_TIN.get());
                        output.accept(ItemRegistry.TIN_INGOT.get());
                        output.accept(ItemRegistry.CRUSHED_IRON_ORE.get());
                        output.accept(ItemRegistry.IRON_DUST.get());
                        output.accept(ItemRegistry.CRUSHED_GOLD_ORE.get());
                        output.accept(ItemRegistry.GOLD_DUST.get());
                        output.accept(ItemRegistry.CRUSHED_COPPER_ORE.get());
                        output.accept(ItemRegistry.COPPER_DUST.get());
                        output.accept(ItemRegistry.CRUSHED_TIN_ORE.get());
                        output.accept(ItemRegistry.TIN_DUST.get());
                        output.accept(ItemRegistry.RUBBER_WOOD.get());
                        output.accept(ItemRegistry.RUBBER_SAPLING.get());
                        output.accept(ItemRegistry.CROP_STICKS.get());
                        output.accept(ItemRegistry.CROP_SEED.get());
                        output.accept(ItemRegistry.FERTILIZER.get());
                        output.accept(ItemRegistry.HYDRATION_CELL.get());
                        output.accept(ItemRegistry.WEED_EX.get());
                        output.accept(ItemRegistry.CROPNALYZER.get());
                        output.accept(ItemRegistry.CROP_HARVESTER.get());
                        output.accept(ItemRegistry.CROPMATRON.get());
                        output.accept(ItemRegistry.CROP_ANALYZER.get());
                        output.accept(ItemRegistry.WET_CONSTRUCTION_FOAM.get());
                        output.accept(ItemRegistry.CONSTRUCTION_FOAM.get());
                        output.accept(ItemRegistry.REINFORCED_STONE.get());
                        output.accept(ItemRegistry.REINFORCED_GLASS.get());
                        output.accept(ItemRegistry.REINFORCED_PLANKS.get());
                        output.accept(ItemRegistry.REINFORCED_BRICKS.get());
                        output.accept(ItemRegistry.REINFORCED_COBBLESTONE.get());
                        output.accept(ItemRegistry.REINFORCED_CRACKED_STONE.get());
                        output.accept(ItemRegistry.REINFORCED_CLEAR_GLASS.get());
                        output.accept(ItemRegistry.REINFORCED_DOOR.get());
                        output.accept(ItemRegistry.WOODEN_SCAFFOLD.get());
                        output.accept(ItemRegistry.IRON_SCAFFOLD.get());
                        output.accept(ItemRegistry.TIN_CAN.get());
                        output.accept(ItemRegistry.FILLED_TIN_CAN.get());
                        output.accept(ItemRegistry.AUTO_FEEDER_MODULE.get());
                        output.accept(ItemRegistry.FOOD_STORAGE_MODULE.get());
                        output.accept(ItemRegistry.JETPACK_MODULE.get());
                        output.accept(ItemRegistry.ENERGY_SHIELD_MODULE.get());
                        output.accept(ItemRegistry.PORTABLE_ENERGY_PACK_MODULE.get());
                        output.accept(ItemRegistry.LAPOTRON_ENERGY_PACK_MODULE.get());
                        output.accept(ItemRegistry.PAINTER.get());
                        output.accept(ItemRegistry.FOAM_PELLET.get());
                        output.accept(ItemRegistry.FOAM_SPRAYER.get());
                        output.accept(ItemRegistry.ELECTRIC_FOAM_SPRAYER.get());
                        output.accept(ItemRegistry.OBSCURATOR.get());
                        output.accept(ItemRegistry.BREWING_BARREL.get());
                        output.accept(ItemRegistry.BEER.get());
                        output.accept(ItemRegistry.RUM.get());
                        output.accept(ItemRegistry.WHISKY.get());
                        output.accept(ItemRegistry.BREWED_POTION.get());
                        output.accept(ItemRegistry.HOPS.get());
                        output.accept(ItemRegistry.TERRA_WART.get());
                        output.accept(ItemRegistry.RUBBER_LEAVES.get());
                        output.accept(ItemRegistry.FLUID_CELL.get());
                        output.accept(ItemRegistry.STICKY_RESIN.get());
                        output.accept(ItemRegistry.RUBBER.get());
                        output.accept(ItemRegistry.BRONZE_INGOT.get());
                        output.accept(ItemRegistry.IRON_PLATE.get());
                        output.accept(ItemRegistry.COPPER_PLATE.get());
                        output.accept(ItemRegistry.TIN_PLATE.get());
                        output.accept(ItemRegistry.BRONZE_PLATE.get());
                        output.accept(ItemRegistry.MIXED_METAL_INGOT.get());
                        output.accept(ItemRegistry.ADVANCED_ALLOY.get());
                        output.accept(ItemRegistry.CARBON_FIBER.get());
                        output.accept(ItemRegistry.RAW_CARBON_MESH.get());
                        output.accept(ItemRegistry.CARBON_PLATE.get());
                        output.accept(ItemRegistry.BASIC_MACHINE_CASING.get());
                        output.accept(ItemRegistry.ADVANCED_MACHINE_CASING.get());
                    })
                    .build());

    private CreativeTabRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void register(final IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
