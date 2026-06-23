# Credits — Textures & Assets

IC2 Port is **not affiliated** with IndustrialCraft² or IC2 Classic.

## Original assets

Textures under `assets/ic2port/textures/` that are not listed below are part of this mod
or were adapted for IC2 Port.

## Third-party texture sources

Bundling policy is defined in `LICENSE-ASSETS.md`:

- Tier Green: safe to bundle in release JAR
- Tier Yellow: conditional (permission required)
- Tier Red: reference-only

### Tech Reborn (primary IC2-style source)

- **Repository:** https://github.com/TechReborn/TechReborn (branch `26.2`)
- **License:** MIT License
- **Tier:** Green
- **Used for:** machines, cables, energy storage, transformers, generators, nuclear
  components, most ingots/dusts/plates, rubber tree, quantum armor, tools, upgrades, etc.

Copyright (c) Tech Reborn contributors. See the Tech Reborn repository for the full MIT
license text.

### Modern Industrialization

- **Repository:** https://github.com/AztechMC/Modern-Industrialization (branch `1.21.x`)
- **License:** CC0 (assets)
- **Tier:** Green
- **Used for:** `uranium_ingot`, `uranium_plate`, `plutonium`, `depleted_uranium`,
  `mox_fuel_rod`, crushed-ore template, `toolbox`, `electric_jetpack`.

### LogicWorlds IC2ClassicFaithful32

- **Repository:** https://github.com/logixy/IC2ClassicFaithful32
- **License:** Unlicense
- **Tier:** Green
- **Status:** Evaluated as legally reusable source for optional IC2C-style mappings,
  especially when newer F32 sources are unavailable for redistribution.

### IC2 Classic faithful (CrossVas F32 addon)

- **Repository:** https://github.com/CrossVas/F32AddonsIC2Classic (`F32-1.19.2` branch paths)
- **License:** No explicit redistribution license; repo README marks pack as personal/non-official
- **Tier:** Yellow (conditional)
- **Used for:** `steam_still` / `steam_flow`, hazmat item + armor layer textures,
  `capacitor_cell` (quantum accumulator icon)
- **Rationale:** Closest publicly extractable match to IC2 Classic originals (per
  [IC2C wiki resource packs](https://github.com/TinyModularThings/IC2Classic/wiki/1.19.x-ResourcePacks)).
  See [docs/ASSET_SOURCES.md](docs/ASSET_SOURCES.md).
- **Publication rule:** Bundle only with explicit written permission; otherwise keep in
  companion pack or replace with Tier Green alternatives.
- **Current repository default:** Base mod JAR uses Tier Green replacements for these slots.

### IC2 Classic / IC2 Experimental originals

- **Source:** CurseForge IC2 Classic and historical IC2 Experimental assets
- **License:** All Rights Reserved
- **Tier:** Red (reference-only)
- **Rule:** Not bundled into IC2 Port; used only as visual reference.

### Interim placeholders

- **Composite armor** (`composite_*`): Tech Reborn peridot armor as advanced-alloy stand-in.
- **Contaminated soil:** based on Tech Reborn uranium ore texture (temporary).
- **New machines (Phase 3–5):** advanced machine casing placeholder block models until unique art is imported.

Bronze armor uses Tech Reborn bronze armor textures (import via `scripts/import-textures.ps1`).
Hazmat uses IC2 Classic faithful textures when import succeeds; otherwise Tech Reborn nano fallback.

## Re-importing assets

```powershell
powershell -ExecutionPolicy Bypass -File scripts/import-textures.ps1
```
