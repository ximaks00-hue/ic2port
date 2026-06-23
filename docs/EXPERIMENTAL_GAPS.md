# Experimental Feature Gap Audit

Tracks parity gaps between IC2 Port and IC2 Classic / Experimental reference targets for post-v1 work.

**Scale reference (verified):** 115 blocks in `BlockRegistry` · 260 items · 322 recipes.

## Phase 7 — Steam reactor & EU net v2

| Feature | Status | Gap |
|---------|--------|-----|
| Steam reactor | MVP | No player GUI; automation-only 9×6 grid |
| Dedicated steam fluid | MVP | Uses water as steam stand-in |
| EU net v2 (`WorldEnergyNet`) | Done | Global per-dimension graph, cross-chunk flood-fill, active-set tick; disable via `globalEnergyNetEnabled` |
| Addon API v1 | Done | `EnergyNet.get(level)`, crops, personal storage hooks |
| Addon recipe runtime | Partial | `IAddonRecipeRegistry` not wired into all machine types |

## Phase 8 — Fluid network

| Feature | Status | Gap |
|---------|--------|-----|
| Fluid pipes | MVP | No wrench disconnect, no cover system |
| Fluid pump | MVP | No EU cost; single-face pull |
| Void pipe | Done | — |
| Fluid-O-Mat | MVP | Personal-tank link + coin trade for filled cells; no owner price GUI packet |

## Phase 9 — Induction matrix

| Feature | Status | Gap |
|---------|--------|-----|
| 5×5×5 validator | Done | Hollow interior only; no capacitor cells |
| Matrix storage | MVP | Capacity scales with casing count; no loss tiers |
| Multiblock GUI | Missing | No cell management screen |

## Phase 10 — Platform

| Feature | Status | Gap |
|---------|--------|-----|
| `IWrenchable` | Partial | Implemented on core machines; not on all ic2port blocks |
| `ScrapBoxEvent` | Done | — |
| GameTest / in-world CI | Missing | Unit tests only; no Forge GameTest harness |
| CI lang parity | Done | Flat JSON keys only |
| Javadoc | Partial | Internal packages undocumented by design |

## Assets

| Area | Status | Notes |
|------|--------|-------|
| Bronze / composite armor | Improved | Tech Reborn stand-in textures via `import-textures.ps1` |
| Hazmat suit | Placeholder | TR nano armor interim |
| New machines (tubes, matrix, etc.) | Placeholder | Often advanced machine casing until unique art |

_Last updated: Phase 5 post-v1 polish / pre-release audit._
