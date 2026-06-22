# IC2 Port — Balance Reference (Phase 5)

Tunable values live in `config/ic2port-common.toml` under `[energy]`, `[balance]`, and `[debug]`.

## Voltage tiers

| Tier | Max packet (EU/t) | Typical storage |
|------|-------------------|-----------------|
| LV   | 32                | BatBox — 40k EU  |
| MV   | 128               | MFE — 600k EU    |
| HV   | 512               | MFSU — 10M EU    |
| EV   | 2048              | ESU — 10M EU     |

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

## Nuclear balance

- **Meltdown heat:** `reactorMaxHeat` (default 10,000) + reactor plating bonus
- **Warning effects:** ignite nearby blocks at 50% heat (`reactorHeatWarningRatio`)
- **Radiation:** players within radius at 75% heat (`reactorHeatRadiationRatio`)
- **Explosion power:** 4 + fuel bonus (capped +8)

## Fusion balance

- **Heat-up cost:** `fusionHeatEuPerTick` (default 128 EU/t) until 200k heat
- **Lava production:** 50 mb/cycle per uranium rod, 75 mb for MOX; × `fusionLavaMultiplier`
- **Production interval:** 20 ticks; fuel consumed every 200 ticks

## Debug profiling

Set `reactorProfilingEnabled = true` in config to log fission reactor ticks slower than `reactorProfilingThresholdMs` (default 5 ms).
