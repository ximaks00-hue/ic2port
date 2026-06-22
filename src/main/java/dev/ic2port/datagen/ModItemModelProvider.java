package dev.ic2port.datagen;

import dev.ic2port.Reference;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(final PackOutput output, final ExistingFileHelper existingFileHelper) {
        super(output, Reference.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        generatedItem(ItemRegistry.RE_BATTERY);
        generatedItem(ItemRegistry.WRENCH);
        generatedItem(ItemRegistry.TREE_TAP);
        generatedItem(ItemRegistry.ELECTRIC_TREE_TAP);
        generatedItem(ItemRegistry.ADVANCED_TREE_TAP);
        generatedItem(ItemRegistry.TIN_INGOT);
        generatedItem(ItemRegistry.RAW_TIN);
        generatedItem(ItemRegistry.RAW_URANIUM);
        generatedItem(ItemRegistry.URANIUM_INGOT);
        generatedItem(ItemRegistry.URANIUM_PLATE);
        generatedItem(ItemRegistry.EMPTY_FUEL_ROD);
        generatedItem(ItemRegistry.FUEL_ROD);
        generatedItem(ItemRegistry.DEPLETED_FUEL_ROD);
        generatedItem(ItemRegistry.HEAT_VENT);
        generatedItem(ItemRegistry.ADVANCED_HEAT_VENT);
        generatedItem(ItemRegistry.OVERCLOCKED_HEAT_VENT);
        generatedItem(ItemRegistry.HEAT_EXCHANGER);
        generatedItem(ItemRegistry.ADVANCED_HEAT_EXCHANGER);
        generatedItem(ItemRegistry.COOLANT_CELL);
        generatedItem(ItemRegistry.TRIPLE_COOLANT_CELL);
        generatedItem(ItemRegistry.QUAD_COOLANT_CELL);
        generatedItem(ItemRegistry.DEPLETED_URANIUM);
        generatedItem(ItemRegistry.PLUTONIUM);
        generatedItem(ItemRegistry.MOX_FUEL_ROD);
        generatedItem(ItemRegistry.LAPOTRON_CRYSTAL);
        generatedItem(ItemRegistry.SCRAP);
        generatedItem(ItemRegistry.SCRAP_BOX);
        generatedItem(ItemRegistry.CENTRIFUGE_ROTOR);
        generatedItem(ItemRegistry.OD_SCANNER);
        generatedItem(ItemRegistry.THERMOMETER);
        generatedItem(ItemRegistry.EU_READER);
        generatedItem(ItemRegistry.CROP_SEED);
        generatedItem(ItemRegistry.CROP_STICKS);
        generatedItem(ItemRegistry.FERTILIZER);
        generatedItem(ItemRegistry.CROPNALYZER);
        generatedItem(ItemRegistry.CROP_HARVESTER);
        generatedItem(ItemRegistry.CROPMATRON);
        generatedItem(ItemRegistry.UU_MATTER);
        generatedItem(ItemRegistry.IRIDIUM);
        generatedItem(ItemRegistry.IRIDIUM_PLATE);
        generatedItem(ItemRegistry.QUANTUM_HELMET);
        generatedItem(ItemRegistry.QUANTUM_CHESTPLATE);
        generatedItem(ItemRegistry.QUANTUM_LEGGINGS);
        generatedItem(ItemRegistry.QUANTUM_BOOTS);
        generatedItem(ItemRegistry.HAZMAT_HELMET);
        generatedItem(ItemRegistry.HAZMAT_CHESTPLATE);
        generatedItem(ItemRegistry.HAZMAT_LEGGINGS);
        generatedItem(ItemRegistry.HAZMAT_BOOTS);
        generatedItem(ItemRegistry.NANO_HELMET);
        generatedItem(ItemRegistry.NANO_CHESTPLATE);
        generatedItem(ItemRegistry.NANO_LEGGINGS);
        generatedItem(ItemRegistry.NANO_BOOTS);
        generatedItem(ItemRegistry.CRUSHED_IRON_ORE);
        generatedItem(ItemRegistry.IRON_DUST);
        generatedItem(ItemRegistry.CRUSHED_GOLD_ORE);
        generatedItem(ItemRegistry.GOLD_DUST);
        generatedItem(ItemRegistry.CRUSHED_COPPER_ORE);
        generatedItem(ItemRegistry.COPPER_DUST);
        generatedItem(ItemRegistry.CRUSHED_TIN_ORE);
        generatedItem(ItemRegistry.TIN_DUST);
        generatedItem(ItemRegistry.STICKY_RESIN);
        generatedItem(ItemRegistry.RUBBER);
        generatedItem(ItemRegistry.BRONZE_INGOT);
        generatedItem(ItemRegistry.IRON_PLATE);
        generatedItem(ItemRegistry.COPPER_PLATE);
        generatedItem(ItemRegistry.TIN_PLATE);
        generatedItem(ItemRegistry.BRONZE_PLATE);
        generatedItem(ItemRegistry.MIXED_METAL_INGOT);
        generatedItem(ItemRegistry.ADVANCED_ALLOY);
        generatedItem(ItemRegistry.CARBON_FIBER);
        generatedItem(ItemRegistry.RAW_CARBON_MESH);
        generatedItem(ItemRegistry.CARBON_PLATE);
        generatedItem(ItemRegistry.DIAMOND_DRILL);
        generatedItem(ItemRegistry.ADVANCED_DRILL);
        generatedItem(ItemRegistry.ELECTRIC_JETPACK);
        generatedItem(ItemRegistry.BATPACK);
        generatedItem(ItemRegistry.LAPPACK);
        generatedItem(ItemRegistry.ENERGY_CRYSTAL);
        generatedItem(ItemRegistry.TOOLBOX);
        generatedItem(ItemRegistry.OVERCLOCKER_UPGRADE);
        generatedItem(ItemRegistry.TRANSFORMER_UPGRADE);
        generatedItem(ItemRegistry.ENERGY_STORAGE_UPGRADE);
    }

    private void generatedItem(final net.minecraftforge.registries.RegistryObject<net.minecraft.world.item.Item> item) {
        String path = item.getId().getPath();
        singleTexture(path, mcLoc("item/generated"), "layer0", modLoc("item/" + path));
    }
}
