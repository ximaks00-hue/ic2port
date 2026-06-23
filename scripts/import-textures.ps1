# Imports IC2-like textures from Tech Reborn (MIT) and Modern Industrialization (CC0).
$ErrorActionPreference = "Stop"
$base = Join-Path $PSScriptRoot "..\src\main\resources\assets\ic2port\textures"
$tr = "https://raw.githubusercontent.com/TechReborn/TechReborn/26.2/src/main/resources/assets/techreborn/textures"
$mi = "https://raw.githubusercontent.com/AztechMC/Modern-Industrialization/1.21.x"

function Get-Asset {
    param([string]$Url, [string]$Dest)
    $dir = Split-Path $Dest -Parent
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    if (Test-Path $Dest) { return }
    Write-Host "GET $Dest"
    try {
        Invoke-WebRequest -Uri $Url -OutFile $Dest -Headers @{ "User-Agent" = "ic2port-asset-import" }
    } catch {
        Write-Host "MISS $Dest"
    }
}

function Get-Tr {
    param([string]$Path, [string]$Dest)
    Get-Asset "$tr/$Path" (Join-Path $base $Dest)
}

function Copy-Fallback {
    param([string]$SourceRel, [string]$DestRel)
    $dest = Join-Path $base $DestRel
    if (Test-Path $dest) { return }
    $source = Join-Path $base $SourceRel
    if (Test-Path $source) {
        $dir = Split-Path $dest -Parent
        if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
        Copy-Item $source $dest
        Write-Host "COPY $DestRel <- $SourceRel"
    }
}

function Get-Mi {
    param([string]$Path, [string]$Dest)
    Get-Asset "$mi/$Path" (Join-Path $base $Dest)
}

# --- Ores & world ---
Get-Tr "block/ore/tin_ore.png" "block/tin_ore.png"
Get-Tr "block/ore/deepslate/deepslate_tin_ore.png" "block/deepslate_tin_ore.png"
Get-Tr "block/ore/uranium_ore.png" "block/uranium_ore.png"
Get-Tr "block/ore/deepslate/deepslate_uranium_ore.png" "block/deepslate_uranium_ore.png"
Get-Tr "block/ore/uranium_ore.png" "block/contaminated_soil.png"

# --- Rubber tree ---
Get-Tr "block/misc/rubber_log.png" "block/rubber_wood.png"
Get-Tr "block/misc/rubber_log_top.png" "block/rubber_wood_top.png"
Get-Tr "block/misc/rubber_log_sap.png" "block/rubber_wood_resin.png"
Get-Tr "block/misc/rubber_leaves.png" "block/rubber_leaves.png"
Get-Tr "block/misc/rubber_sapling.png" "block/rubber_sapling.png"

# --- Casings ---
Get-Tr "block/machines/structure/basic_machine_casing.png" "block/basic_machine_casing.png"
Get-Tr "block/machines/structure/advanced_machine_casing.png" "block/advanced_machine_casing.png"

# --- Cables ---
Get-Tr "block/cables/copper_cable.png" "block/copper_cable.png"
Get-Tr "block/cables/insulated_gold_cable.png" "block/gold_cable.png"
Get-Tr "block/cables/insulated_hv_cable.png" "block/hv_cable.png"
Get-Tr "block/cables/glassfiber_cable.png" "block/glass_fiber_cable.png"
Get-Tr "item/cables/copper_cable.png" "item/copper_cable.png"
Get-Tr "item/cables/insulated_gold_cable.png" "item/gold_cable.png"
Get-Tr "item/cables/insulated_hv_cable.png" "item/hv_cable.png"
Get-Tr "item/cables/glassfiber_cable.png" "item/glass_fiber_cable.png"

# --- Energy storage ---
Get-Tr "block/machines/energy/low_voltage_su_front.png" "block/batbox_front.png"
Get-Tr "block/machines/energy/low_voltage_su_side.png" "block/batbox_side.png"
Get-Tr "block/machines/energy/medium_voltage_su_front.png" "block/mfe_front.png"
Get-Tr "block/machines/energy/medium_voltage_su_side.png" "block/mfe_side.png"
Get-Tr "block/machines/energy/high_voltage_su_front.png" "block/mfsu_front.png"
Get-Tr "block/machines/energy/high_voltage_su_side.png" "block/mfsu_side.png"

# --- Transformers ---
Get-Tr "block/machines/energy/lv_transformer_front.png" "block/lv_transformer_front.png"
Get-Tr "block/machines/energy/lv_transformer_side.png" "block/lv_transformer_side.png"
Get-Tr "block/machines/energy/mv_transformer_front.png" "block/mv_transformer_front.png"
Get-Tr "block/machines/energy/mv_transformer_side.png" "block/mv_transformer_side.png"
Get-Tr "block/machines/energy/ev_transformer_front.png" "block/ev_transformer_front.png"
Get-Tr "block/machines/energy/ev_transformer_side.png" "block/ev_transformer_side.png"

# --- Machines ---
Get-Tr "block/machines/tier1_machines/grinder_front_off.png" "block/macerator_front.png"
Get-Tr "block/machines/tier1_machines/grinder_top_off.png" "block/macerator_top.png"
Get-Tr "block/machines/tier1_machines/compressor_front_off.png" "block/compressor_front.png"
Get-Tr "block/machines/tier1_machines/extractor_front_off.png" "block/extractor_front.png"
Get-Tr "block/machines/tier1_machines/electric_furnace_front_off.png" "block/electric_furnace_front.png"
Get-Tr "block/machines/tier1_machines/alloy_smelter_front_off.png" "block/induction_furnace_front.png"
Copy-Fallback "block/extractor_front.png" "block/electric_furnace_front.png"
Copy-Fallback "block/thermal_centrifuge_front.png" "block/induction_furnace_front.png"
Get-Tr "block/machines/tier1_machines/former_front_off.png" "block/metal_former_front.png"
Copy-Fallback "block/compressor_front.png" "block/metal_former_front.png"
Copy-Fallback "block/advanced_machine_casing.png" "block/metal_former_side.png"
Get-Tr "block/machines/tier1_machines/recycler_front_off.png" "block/recycler_front.png"
Copy-Fallback "block/compressor_front.png" "block/recycler_front.png"
Get-Tr "block/machines/tier2_machines/industrial_centrifuge_front_off.png" "block/thermal_centrifuge_front.png"
Get-Tr "block/machines/tier2_machines/industrial_centrifuge_top_off.png" "block/thermal_centrifuge_top.png"
Get-Tr "block/machines/tier3_machines/matter_fabricator_side_off.png" "block/mass_fabricator_side.png"
Get-Tr "block/machines/tier3_machines/matter_fabricator_top_off.png" "block/mass_fabricator_top.png"
Get-Tr "block/machines/tier3_machines/matter_fabricator_bottom.png" "block/mass_fabricator_bottom.png"

# --- Generators ---
Get-Tr "block/machines/tier0_machines/iron_furnace_front_off.png" "block/solid_fuel_generator_front.png"
Get-Tr "block/machines/tier0_machines/iron_furnace_front_on.png" "block/solid_fuel_generator_front_on.png"
Get-Tr "block/machines/generators/thermal_generator_side_off.png" "block/geothermal_generator_side.png"
Get-Tr "block/machines/generators/thermal_generator_side_on.png" "block/geothermal_generator_side_on.png"
Get-Tr "block/machines/generators/thermal_generator_top_off.png" "block/geothermal_generator_top.png"
Get-Tr "block/machines/generators/thermal_generator_top_on.png" "block/geothermal_generator_top_on.png"
Get-Tr "block/machines/generators/solar_panel_top.png" "block/solar_panel_top.png"
Get-Tr "block/machines/generators/solar_panel_side_off.png" "block/solar_panel_side.png"
Get-Tr "block/machines/generators/wind_mill_front.png" "block/wind_mill_front.png"
Get-Tr "block/machines/generators/wind_mill_top.png" "block/wind_mill_top.png"
Get-Tr "block/machines/generators/wind_mill_bottom.png" "block/wind_mill_bottom.png"
Get-Tr "block/machines/generators/water_mill_top_off.png" "block/water_mill_top.png"
Get-Tr "block/machines/generators/water_mill_side_off.png" "block/water_mill_side.png"
Get-Tr "block/machines/generators/creative_solar_panel_top.png" "block/creative_generator.png"
Get-Tr "block/machines/generators/plasma_generator_top_off.png" "block/hv_creative_generator_top.png"
Get-Tr "block/machines/generators/plasma_generator_side_off.png" "block/hv_creative_generator_side.png"

# --- Nuclear ---
Get-Tr "block/machines/generators/nuclear/nuclear_reactor_front_off.png" "block/nuclear_reactor_front.png"
Get-Tr "block/machines/generators/nuclear/nuclear_reactor_side.png" "block/nuclear_reactor_side.png"
Get-Tr "block/machines/generators/nuclear/nuclear_reactor_top.png" "block/nuclear_reactor_top.png"
Get-Tr "block/machines/generators/nuclear/reactor_chamber.png" "block/reactor_chamber.png"

# --- Items: metals & dusts ---
Get-Tr "item/rawmetal/raw_tin.png" "item/raw_tin.png"
Get-Tr "item/rawmetal/raw_uranium.png" "item/raw_uranium.png"
Get-Tr "item/ingot/tin_ingot.png" "item/tin_ingot.png"
Get-Tr "item/ingot/bronze_ingot.png" "item/bronze_ingot.png"
Get-Mi "src/generated/resources/assets/modern_industrialization/textures/item/uranium_ingot.png" "item/uranium_ingot.png"
Get-Tr "item/dust/iron_dust.png" "item/iron_dust.png"
Get-Tr "item/dust/gold_dust.png" "item/gold_dust.png"
Get-Tr "item/dust/copper_dust.png" "item/copper_dust.png"
Get-Tr "item/dust/tin_dust.png" "item/tin_dust.png"
Get-Tr "item/plate/iron_plate.png" "item/iron_plate.png"
Get-Tr "item/plate/copper_plate.png" "item/copper_plate.png"
Get-Tr "item/plate/tin_plate.png" "item/tin_plate.png"
Get-Tr "item/plate/bronze_plate.png" "item/bronze_plate.png"
Get-Mi "src/generated/resources/assets/modern_industrialization/textures/item/mixed_plate_nuclear.png" "item/uranium_plate.png"
Get-Tr "item/ingot/mixed_metal_ingot.png" "item/mixed_metal_ingot.png"
Get-Tr "item/ingot/advanced_alloy_ingot.png" "item/advanced_alloy.png"
Get-Tr "item/part/carbon_fiber.png" "item/carbon_fiber.png"
Get-Tr "item/part/carbon_mesh.png" "item/raw_carbon_mesh.png"
Get-Tr "item/plate/carbon_plate.png" "item/carbon_plate.png"
Get-Tr "item/ingot/iridium_ingot.png" "item/iridium.png"
Get-Tr "item/plate/iridium_plate.png" "item/iridium_plate.png"
Get-Mi "src/generated/resources/assets/modern_industrialization/textures/item/plutonium_ingot.png" "item/plutonium.png"
Get-Mi "src/generated/resources/assets/modern_industrialization/textures/item/plutonium_nugget.png" "item/depleted_uranium.png"

# --- Rubber & resin ---
Get-Tr "item/part/sap.png" "item/sticky_resin.png"
Get-Tr "item/tool/thermometer.png" "item/thermometer.png"
Copy-Fallback "item/wrench.png" "item/thermometer.png"
Copy-Fallback "item/sticky_resin.png" "item/tree_tap.png"
Copy-Fallback "item/tree_tap.png" "item/electric_tree_tap.png"
Get-Tr "item/part/rubber.png" "item/rubber.png"

# --- Crushed ores (keep style: TR dust pile recolored via existing if present) ---
Get-Mi "src/main/resources/assets/modern_industrialization/textures/materialsets/common/crushed_dust.png" "item/crushed_iron_ore.png"
Get-Mi "src/main/resources/assets/modern_industrialization/textures/materialsets/common/crushed_dust.png" "item/crushed_gold_ore.png"
Get-Mi "src/main/resources/assets/modern_industrialization/textures/materialsets/common/crushed_dust.png" "item/crushed_copper_ore.png"
Get-Mi "src/main/resources/assets/modern_industrialization/textures/materialsets/common/crushed_dust.png" "item/crushed_tin_ore.png"

# --- Batteries & matter ---
Get-Tr "item/battery/red_cell_battery.png" "item/re_battery.png"
Get-Tr "item/battery/energy_crystal.png" "item/energy_crystal.png"
Get-Tr "item/battery/lapotron_crystal.png" "item/lapotron_crystal.png"
Get-Tr "item/part/scrap.png" "item/scrap.png"
Get-Tr "item/part/scrapbox.png" "item/scrap_box.png"
Copy-Fallback "item/scrap.png" "item/scrap_box.png"
Get-Tr "item/part/uu_matter.png" "item/uu_matter.png"

# --- Tools & upgrades ---
Get-Tr "item/tool/wrench.png" "item/wrench.png"
Get-Tr "item/tool/od_scanner.png" "item/od_scanner.png"
Copy-Fallback "item/energy_crystal.png" "item/od_scanner.png"
Get-Tr "item/part/rotor_carbon.png" "item/centrifuge_rotor.png"
Copy-Fallback "item/iron_plate.png" "item/centrifuge_rotor.png"
Get-Tr "item/tool/basic_drill.png" "item/diamond_drill.png"
Get-Tr "item/tool/advanced_drill.png" "item/advanced_drill.png"
Get-Tr "item/upgrade/overclocker_upgrade.png" "item/overclocker_upgrade.png"
Get-Tr "item/upgrade/transformer_upgrade.png" "item/transformer_upgrade.png"
Get-Tr "item/upgrade/energy_storage_upgrade.png" "item/energy_storage_upgrade.png"
Get-Mi "src/main/resources/assets/modern_industrialization/textures/item/portable_storage_unit.png" "item/toolbox.png"
Get-Mi "src/main/resources/assets/modern_industrialization/textures/item/diesel_jetpack.png" "item/electric_jetpack.png"

# --- Armor & packs ---
Get-Tr "item/armor/lithium_batpack.png" "item/batpack.png"
Get-Tr "item/armor/lapotronic_orbpack.png" "item/lappack.png"
Get-Tr "item/armor/quantum_helmet.png" "item/quantum_helmet.png"
Get-Tr "item/armor/quantum_chestplate.png" "item/quantum_chestplate.png"
Get-Tr "item/armor/quantum_leggings.png" "item/quantum_leggings.png"
Get-Tr "item/armor/quantum_boots.png" "item/quantum_boots.png"
# Hazmat: TR nano armor (rubber path not in TR 26.2; interim until custom art)
Get-Tr "item/armor/nano_helmet.png" "item/hazmat_helmet.png"
Get-Tr "item/armor/nano_chestplate.png" "item/hazmat_chestplate.png"
Get-Tr "item/armor/nano_leggings.png" "item/hazmat_leggings.png"
Get-Tr "item/armor/nano_boots.png" "item/hazmat_boots.png"
# Bronze & composite armor (TR bronze / peridot as advanced-alloy stand-in)
Get-Tr "item/armor/bronze_helmet.png" "item/bronze_helmet.png"
Get-Tr "item/armor/bronze_chestplate.png" "item/bronze_chestplate.png"
Get-Tr "item/armor/bronze_leggings.png" "item/bronze_leggings.png"
Get-Tr "item/armor/bronze_boots.png" "item/bronze_boots.png"
Get-Tr "item/armor/peridot_helmet.png" "item/composite_helmet.png"
Get-Tr "item/armor/peridot_chestplate.png" "item/composite_chestplate.png"
Get-Tr "item/armor/peridot_leggings.png" "item/composite_leggings.png"
Get-Tr "item/armor/peridot_boots.png" "item/composite_boots.png"

# --- Nuclear components ---
Get-Tr "item/part/nuclear/empty_fuel_rod.png" "item/empty_fuel_rod.png"
Get-Tr "item/part/nuclear/uranium_fuel_rod.png" "item/fuel_rod.png"
Get-Tr "item/part/nuclear/depleted_uranium_fuel_rod.png" "item/depleted_fuel_rod.png"
Get-Mi "src/generated/resources/assets/modern_industrialization/textures/item/le_mox_fuel_rod.png" "item/mox_fuel_rod.png"
Get-Tr "item/part/nuclear/heat_vent.png" "item/heat_vent.png"
Get-Tr "item/part/nuclear/advanced_heat_vent.png" "item/advanced_heat_vent.png"
Get-Tr "item/part/nuclear/overclocked_heat_vent.png" "item/overclocked_heat_vent.png"
Get-Tr "item/part/nuclear/heat_exchanger.png" "item/heat_exchanger.png"
Get-Tr "item/part/nuclear/advanced_heat_exchanger.png" "item/advanced_heat_exchanger.png"
Get-Tr "item/part/nuclear/water_coolant_cell_10k.png" "item/coolant_cell.png"
Get-Tr "item/part/nuclear/water_coolant_cell_30k.png" "item/triple_coolant_cell.png"
Get-Tr "item/part/nuclear/water_coolant_cell_60k.png" "item/quad_coolant_cell.png"

Write-Host "Done."
