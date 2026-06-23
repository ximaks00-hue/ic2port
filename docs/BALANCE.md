# IC2 Port — Balance Reference

Tunable values live in `config/ic2port-common.toml` under `[energy]`, `[balance]`, and `[debug]`.

## Voltage tiers

| Tier | Max packet (EU/t) | Typical storage |
|------|-------------------|-----------------|
| LV   | 32                | BatBox — 40k EU  |
| MV   | 128               | MFE — 600k EU    |
| HV   | 512               | MFSU — 10M EU    |
| EV   | 2048              | ESU — 10M EU     |

## EU storage blocks

| Block | Capacity | Max output (EU/t) | Tier |
|-------|----------|-------------------|------|
| BatBox | 40k | 32 | LV |
| MFE | 600k | 128 | MV |
| MFSU | 10M | 512 | HV |
| ESU | 10M | 2048 | EV |
| PESU | 100M | 8192 | EV |
| ISU | 1B | 32768 | EV |

Capacities for BatBox/MFE/MFSU are also configurable in `[energy]`; ESU/PESU/ISU use fixed constants in code.

## Generators (EU/t, typical)

| Block | Output | Tier | Notes |
|-------|--------|------|-------|
| Solid fuel generator | 10 | LV | Coal/charcoal |
| Geothermal | ~20 | LV | Lava buffer |
| Solar panel | 1 | LV | Day, clear sky |
| Advanced solar | 8 | MV | Day, clear sky |
| HV solar | 32 | HV | Day, clear sky |
| Water mill | ≤0.75 | LV | Adjacent water blocks |
| Wind mill | ≤2 | LV | Height + obstacles |
| Nuclear reactor | variable | HV | Grid + redstone signal |
| Steam reactor | — | HV | Steam (water) output, no direct EU |
| Fusion reactor | — | HV | Lava production, not direct EU |

## LV machines (2 EU/t unless noted)

Macerator, extractor, compressor, recycler, electric furnace — 4000 EU buffer, ~200 tick default cycle.

## MV machines

| Machine | EU/t | Speed notes |
|---------|------|-------------|
| Induction furnace | 6 | 2× electric furnace lanes |
| Blast induction furnace | 30 | Ores only, 4× lane speed |
| Metal former | 4 | 100 tick default |
| Thermal centrifuge | 8 | 500 tick default |
| Centrifugal extractor | 16 | 2× extractor |
| Electrolyzer | 5 | Dual output |
| Ore washer | 4 | Washed dust output |
| Alloy smelter | 5 | Two inputs |
| Cropmatron | 16/cycle | 9×5×9 area, every 40 ticks |

## HV / EV endgame

| Machine | EU/t | Notes |
|---------|------|-------|
| Mass fabricator | 512 | 1M EU per UU blob |
| Miner | 24 | 3×3 quarry column |
| Pump | 8 | Fluid drain |
| Pattern replicator | 32 | UU + pattern |
| Teleporter | distance-scaled | Linked pads |
| Terraformer | 48 | 9×9 blueprint area |
| Ore scanner (block) | 40 EU/block scanned | 8×8×48 volume |

## Steam reactor (MVP)

| Constant | Value | Notes |
|----------|-------|-------|
| Grid | 9×6 | Same component rules as fission |
| Steam tank | 16,000 mB | Internal buffer |
| Steam production | 100 mB/tick | While active and heated |
| Output | Adjacent fluid handlers | Water-based steam fluid |

## Recycler

| Setting | Default | Notes |
|---------|---------|-------|
| `recyclerScrapChance` | 0.125 (12.5%) | Loose scrap output |
| `recyclerScrapBoxChance` | 0.01 (1%) | Rare scrap box output |

## Nuclear balance

- **Meltdown heat:** `reactorMaxHeat` (default 10,000) + reactor plating bonus
- **Warning effects:** ignite nearby blocks at 50% heat (`reactorHeatWarningRatio`)
- **Radiation:** players within radius at 75% heat (`reactorHeatRadiationRatio`)
- **Explosion power:** 4 + fuel bonus (capped +8)

## Fusion balance

- **Heat-up cost:** `fusionHeatEuPerTick` (default 128 EU/t) until 200k heat
- **Lava production:** 50 mb/cycle per uranium rod, 75 mb for MOX; × `fusionLavaMultiplier`
- **Production interval:** 20 ticks; fuel consumed every 200 ticks

## EU network performance

**Global energy net (v2)** — enabled by default (`globalEnergyNetEnabled` in `[energy]`):

| Component | Role |
|-----------|------|
| `WorldEnergyNet` | Per-dimension registry of all cable conductors |
| `EnergyGrid` | Flood-filled connected component (cross-chunk) with cached acceptor mask |
| Active set | Only cables with buffered EU or recent inject are processed each tick |
| `EnergyNetForgeEvents` | Single `LevelTick` END pass replaces per-cable block entity tickers |

Legacy v1 helpers (`lazy tick`, per-cable mask cache, chunk clustering) remain as fallbacks when v2 is disabled.

## Debug profiling

Optional server-side tick profilers log operations slower than a configurable threshold. All are **disabled by default** (`config/ic2port-common.toml` → `[debug]`).

| Profiler | Config flag | Threshold key | Default threshold |
|----------|-------------|---------------|-------------------|
| Reactor fission / cooldown | `reactorProfilingEnabled` | `reactorProfilingThresholdMs` | 5 ms |
| Cable forward (net tick) | `cableProfilingEnabled` | `cableProfilingThresholdMs` | 2 ms |
| Tube logistics | `tubeProfilingEnabled` | `tubeProfilingThresholdMs` | 5 ms |

In-game (OP level 2): `/ic2port profile` shows status; `/ic2port profile <reactor|cable|tube> <true|false>` toggles profiling for the current session.
