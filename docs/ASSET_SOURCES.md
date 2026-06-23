# Asset source analysis (IC2 Port)

Research summary for textures closest to **IC2 Classic** originals, and what we import.

## Priority sources (by fidelity to IC2 Classic)

Current release-safe baseline uses Tier Green assets in the mod JAR.
CrossVas F32 assets are treated as optional/reference unless explicit permission exists.

| Asset | Best match found | License / notes | Our file |
|-------|------------------|-----------------|----------|
| **Steam fluid** | TechReborn fallback (geothermal textures) | Tier Green safe bundle default | `textures/block/steam_still.png`, `steam_flow.png` |
| **Hazmat icons** | TechReborn nano fallback | Tier Green safe bundle default | `textures/item/hazmat_*.png` |
| **Hazmat worn layers** | TechReborn/CrossVas-independent nano layers | Tier Green safe bundle default | `textures/models/armor/hazmat_layer_*.png` |
| **Capacitor cell** | TechReborn `energy_crystal` fallback | Tier Green safe bundle default | `textures/item/capacitor_cell.png` |
| **Bronze armor** | [TechReborn](https://github.com/TechReborn/TechReborn) MIT | IC2-style bronze | `textures/item/bronze_*.png` |
| **Composite armor** | TechReborn peridot set MIT | Advanced-alloy stand-in | `textures/item/composite_*.png` |
| **Machines / cables / ores** | TechReborn MIT + MI CC0 | General IC2 parity | see `scripts/import-textures.ps1` |

## Sources evaluated but not used as primary

| Source | Why not primary |
|--------|-----------------|
| [TinyModularThings/IC2Classic](https://github.com/TinyModularThings/IC2Classic) | Textures not in git; shipped via mod JAR / wiki zip only |
| [LogicWorlds/IC2ClassicFaithful32](https://github.com/logixy/IC2ClassicFaithful32) | Unlicense, but 1.12.2 pack; CrossVas F32 is newer IC2C 1.19.2 faithful |
| TechReborn nano armor | Was hazmat placeholder; replaced by IC2C faithful hazmat |
| Vanilla tinted water | Was steam placeholder; replaced |
| IC2 Experimental (1.12) steam | Different mod lineage; IC2C F32 steam is closer for Classic |

## Resolution note

CrossVas textures are **32×32** (Faithful scale). Minecraft accepts higher-res assets; they render sharper in GUI/armor. Downscale to 16×16 is optional if pixel-parity with other items is desired.

## Re-import

```powershell
powershell -ExecutionPolicy Bypass -File scripts/import-textures.ps1
```

Use `-Force` paths in script for IC2C assets overwrite TR fallbacks.

## Attribution

See [CREDITS.md](../CREDITS.md) for full license list.
