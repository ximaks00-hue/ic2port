# Experimental Feature Gap Audit

Tracks parity gaps between IC2 Port and IC2 Classic / Experimental reference targets for post-v1 work.

**Scale reference (verified):** 115 blocks in `BlockRegistry` · 260+ items · 322+ recipes.

## Phase 7 — Steam reactor & EU net v2

| Feature | Status | Gap |
|---------|--------|-----|
| Steam reactor GUI | Done | 9×6 grid, heat/steam bars, redstone SCRAM |
| Dedicated steam fluid | Done | `ic2port:steam` + legacy water tank migration |
| EU net v2 (`WorldEnergyNet`) | Done | Global per-dimension graph, cross-chunk flood-fill, active-set tick; disable via `globalEnergyNetEnabled` |
| Addon API v1 | Done | `EnergyNet.get(level)`, crops, personal storage hooks |
| Addon recipe runtime | Done | Wired into macerator, compressor, extractor/centrifugal extractor, electric/induction furnace, electrolyzer, ore washer, metal former, alloy smelter, thermal centrifuge |

## Phase 8 — Fluid network

| Feature | Status | Gap |
|---------|--------|-----|
| Fluid pipes | Done | Wrench face disconnect; shift+wrench cover mask |
| Fluid pump | Done | LV, 100 EU/bucket (0.2 EU/mB), 2k internal buffer |
| Void pipe | Done | — |
| Fluid-O-Mat | Done | Price scroll/keys for owner; personal-tank trade |

## Phase 9 — Induction matrix

| Feature | Status | Gap |
|---------|--------|-----|
| 5×5×5 validator | Done | Hollow interior only |
| Capacitor cells | Done | 3×3 GUI slots; +1M EU per cell |
| Matrix storage | MVP | Capacity scales with casing + cells; no loss tiers |
| Multiblock GUI | Done | Energy bar + structure invalid overlay |

## Phase 10 — Platform

| Feature | Status | Gap |
|---------|--------|-----|
| `IWrenchable` | Done | All `ic2port:*` blocks use defaults via `WrenchableDefaults`; custom hooks on pipes etc. |
| `ScrapBoxEvent` | Done | — |
| GameTest / in-world CI | Baseline | Release-gate sanity suite added (EU chain, machine recipes, steam fluid, teleporter cost); extend scenario coverage in CI |
| CI lang parity | Done | Flat JSON keys only |
| Javadoc | Partial | Internal packages undocumented by design |

## Assets

| Area | Status | Notes |
|------|--------|-------|
| Bronze / composite armor | Improved | Tech Reborn stand-in textures via `import-textures.ps1` |
| Hazmat suit | Improved | Release-safe: Tech Reborn nano fallback in base JAR |
| Capacitor cell | Improved | Release-safe: Tech Reborn energy-crystal fallback icon |
| Steam fluid | Improved | Dedicated `ic2port:steam`; release-safe fallback texture in base JAR |
| New machines (tubes, matrix, etc.) | Placeholder | Often advanced machine casing until unique art |

_Last updated: post-v1 gap closure pass (steam GUI, pipes, matrix, addon recipes)._
