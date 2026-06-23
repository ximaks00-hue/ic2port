# Credits — Textures & Assets

IC2 Port is **not affiliated** with IndustrialCraft² or IC2 Classic.

## Original assets

Textures under `assets/ic2port/textures/` that are not listed below are part of this mod
or were adapted for IC2 Port.

## Third-party texture sources

### Tech Reborn (primary IC2-style source)

- **Repository:** https://github.com/TechReborn/TechReborn (branch `26.2`)
- **License:** MIT License
- **Used for:** machines, cables, energy storage, transformers, generators, nuclear
  components, most ingots/dusts/plates, rubber tree, quantum armor, tools, upgrades, etc.

Copyright (c) Tech Reborn contributors. See the Tech Reborn repository for the full MIT
license text.

### Modern Industrialization

- **Repository:** https://github.com/AztechMC/Modern-Industrialization (branch `1.21.x`)
- **License:** CC0 (assets)
- **Used for:** `uranium_ingot`, `uranium_plate`, `plutonium`, `depleted_uranium`,
  `mox_fuel_rod`, crushed-ore template, `toolbox`, `electric_jetpack`.

### Interim placeholders

- **Hazmat suit** (`hazmat_*`): Tech Reborn nano armor textures (interim until custom art).
- **Composite armor** (`composite_*`): Tech Reborn peridot armor as advanced-alloy stand-in.
- **Contaminated soil:** based on Tech Reborn uranium ore texture (temporary).
- **New machines (Phase 3–5):** advanced machine casing placeholder block models until unique art is imported.

Bronze armor uses Tech Reborn bronze armor textures (import via `scripts/import-textures.ps1`).

## Re-importing assets

```powershell
powershell -ExecutionPolicy Bypass -File scripts/import-textures.ps1
```
