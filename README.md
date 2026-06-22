# IC2 Port

[![Build](https://github.com/ximaks00-hue/ic2port/actions/workflows/build.yml/badge.svg)](https://github.com/ximaks00-hue/ic2port/actions/workflows/build.yml)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green.svg)](https://www.minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.x-orange.svg)](https://files.minecraftforge.net/)
[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://adoptium.net/)

**Industrial Craft 2 reimplementation** for Minecraft **1.20.1** / **Forge 47.x**.

Independent implementation inspired by IC2 Experimental and IC2 Classic — not a fork or merge of either.  
EU energy network, ore processing, nuclear power, crops, nano/quantum armor, and endgame machines from **LV to EV**.

> **Status:** active development · `0.1.0-SNAPSHOT` · [Issues](https://github.com/ximaks00-hue/ic2port/issues) · [Milestones](https://github.com/ximaks00-hue/ic2port/milestones)

---

## Features at a glance

| Category | Highlights |
|----------|------------|
| **Energy** | EU tiers LV–EV · copper/gold/HV/glass-fiber cables · **detector / splitter cables** · LV/MV/EV transformers · BatBox → MFE → MFSU → **ESU** |
| **Generators** | Solid fuel · geothermal · solar (LV + advanced MV + **HV**) · wind · water |
| **Machines** | Macerator · extractor · **centrifugal extractor** · compressor · recycler · electric/**induction blast** furnace · metal former · thermal centrifuge · mass fabricator · canner · vacuum canner · **electrolyzer** · **ore washer** · **alloy smelter** |
| **Automation** | **Miner** (HV) · **Pump** (MV) · crop harvester · cropmatron |
| **Nuclear** | Fission reactor + chambers · MOX · reflectors · plating · condensators · dual/quad fuel rods · meltdown |
| **Fusion** | 5×5×5 shell reactor · lava production · valve export · comparator (heat/lava) · auto-export toggle |
| **Crops** | 18+ species · cross-breeding · fertilizer · cropnalyzer (block + EU handheld) |
| **Brewing** | Barrel: beer · rum · whisky · potions · tin cans |
| **Armor** | **Bronze / composite** · hazmat · nano / quantum suits · 6 chestplate modules · module charge/discharge in storage blocks |
| **Tools** | Drills · chainsaw · electric wrench · tree taps · OD / **OV scanner** · **mining laser** · EU reader · jetpack |
| **Endgame** | UU-matter · **teleporter** · **terraformer** · **pattern replicator** · iridium |
| **World** | Tin & uranium ores · rubber trees · contaminated soil · construction foam |

**Scale:** ~75 blocks · ~185 items · 270+ recipes · 8 advancements · EN + RU localization

---

## Quick start

### Requirements

- **Java 17**
- **Minecraft 1.20.1** + **Forge 47.2+**

### Build & run

```bash
./gradlew build          # compile + JAR
./gradlew runClient      # dev client (JEI included)
./gradlew runData        # regenerate blockstates/models
```

Windows:

```powershell
.\gradlew.bat runClient
```

Import placeholder textures (Tech Reborn MIT + Modern Industrialization CC0):

```powershell
.\scripts\import-textures.ps1
```

See [`CREDITS.md`](CREDITS.md) for asset attribution.

---

## Progression

| Tier | Voltage | Storage | Key unlocks |
|------|---------|---------|-------------|
| **LV** | 32 EU/t | BatBox (40k) | Generator · macerator · extractor · compressor · recycler · electric furnace · RE-battery |
| **MV** | 128 EU/t | MFE (600k) | Induction furnace · blast furnace · centrifugal extractor · metal former · thermal centrifuge · charge pad · nano suit · **ore washer** · **alloy smelter** |
| **HV** | 512 EU/t | MFSU (10M) | Nuclear reactor · fusion reactor · mass fabricator · hazmat · **miner** · **teleporter** |
| **EV** | 2048 EU/t | **ESU (10M)** | Quantum suit · UU-matter · **pattern replicator** · EV transformer |

Typical ore path: **macerator** (×2 crushed) → **ore washer** or **electric furnace** / **blast induction furnace** → plates via **compressor** / **metal former**.

Balance tables and config keys: [`docs/BALANCE.md`](docs/BALANCE.md).

Armor charging: place electric armor in the **top charge slot** of BatBox / MFE / MFSU / ESU.  
Module discharge: place nano/quantum chestplate in the **discharge slot** — modules (batpack, lappack, jetpack) drain into storage.

---

## Notable systems

### EU network

Custom capability-based EU transfer (`IEnergyNode`). Adjacent block energy injection with tier gating.  
Tools: **EU Reader** (instant stats or 20-tick flow average).

### Nuclear reactor

6×6 grid inside reactor + chambers. Heat simulation, component interactions, SCRAM, meltdown with contaminated soil.  
Components: fuel rods, MOX, heat vents/exchangers, coolant cells, neutron reflectors, reactor plating, RSH/LZH condensators, dual/quad uranium rods.

### Fusion reactor

5×5×5 reinforced shell. Uranium/MOX fuel rods → lava + EU. Valve blocks export lava.  
Comparator: lava fill or heat mode. Shift+right-click toggles auto-export.

### Crops

Plant **crop sticks** on farmland. Cross-breed mature neighbors for new species (stickreed, hops, ore crops, etc.).  
**Cropmatron** (9×5×9) auto-waters and applies fertilizer. **Crop harvester** (3×3×3) auto-harvests.  
**Crop analyzer** block or **EU cropnalyzer** handheld reveals stats.

### Construction foam

Wet foam dries into blast-resistant blocks. **Painter** recolors. **CFoam sprayer** (manual + electric).  
Reinforced stone/glass/planks/doors for nuclear shielding.

### Fluids

**Empty fluid cells** (NBT-based) fill/empty in **canner** with water/lava buckets. **Pump** drains fluid sources.

### Endgame

- **Teleporter** + **frequency transmitter** — EU cost scales with distance
- **Terraformer** + blueprints — cultivation / irrigation / desertification (9×9 area)
- **Pattern replicator** — UU-matter + stored pattern → item duplication
- **Mass fabricator** — 1M EU per UU blob

---

## Playtest checklist

```bash
./gradlew runData && ./gradlew runClient
```

JEI (`R`) shows all custom machine recipes under **IC2 Port**.

| Step | Verify |
|------|--------|
| Worldgen | Tin/uranium ores; rubber trees in forest/jungle/swamp |
| LV chain | Rubber → generator → macerator → BatBox → electric furnace |
| MV chain | MFE → metal former → thermal centrifuge → nano suit |
| Crops | Sticks → breeding → cropmatron → harvester → cropnalyzer |
| Nuclear | Reactor GUI, heat, SCRAM, components, meltdown |
| Fusion | Shell build → heat-up → lava → valve → comparator |
| HV tools | Miner digs down; pump drains water/lava |
| Endgame | Teleporter link; terraformer blueprint; pattern replicator + UU |
| Energy | Cables, transformers, ESU, armor/module charge & discharge |

Known gaps: hazmat reuses nano textures; bronze/composite use vanilla armor icons; some assets are TR/MI placeholders. See [`CREDITS.md`](CREDITS.md).

---

## Development

### Project layout

```
src/main/java/dev/ic2port/
├── api/energy/          # EU network interfaces & tiers
├── block/ blockentity/  # Machines, cables, generators, reactors
├── item/                # Tools, armor, reactor parts, materials
├── menu/ client/screen/ # GUIs
├── recipe/              # Custom machine recipe types
├── crop/ brewing/       # Crop registry, brewing logic
├── setup/               # DeferredRegister, events, config
└── datagen/             # runData providers

src/main/resources/
├── assets/ic2port/      # Textures, models, lang (en_us, ru_ru)
└── data/ic2port/        # Recipes, loot, worldgen, advancements, tags

reference/               # IC2 API JAR + ic2-classic source (read-only)
scripts/import-textures.ps1
```

### Adding a new machine

Every GUI machine follows a 6-file pattern. Use **Macerator** as reference:

| File | Role |
|------|------|
| `blockentity/FooBlockEntity.java` | `BaseMachineBlockEntity` — EU, progress, `ContainerData` |
| `block/FooBlock.java` | `BaseEntityBlock` — open GUI, server tick |
| `menu/FooMenu.java` | `MachineWithUpgradesMenu` — slots + sync |
| `client/screen/FooScreen.java` | `MachineScreen` — progress/energy bars |
| `recipe/FooRecipe*.java` | JSON: `input`, `output`, `energy`, `time` |
| Registries | `BlockRegistry`, `BlockEntityRegistry`, `MenuTypeRegistry`, `ItemRegistry`, `ClientModEvents`, lang files |

Full checklist in README history; run `./gradlew compileJava` after each step.

### IC2 reference

Official IC2 source is closed. Read-only references:

```bash
./gradlew downloadIc2ReferenceJars
```

Details: [`reference/docs/IC2_REFERENCE_SETUP.md`](reference/docs/IC2_REFERENCE_SETUP.md)

### Roadmap

Development is tracked in [GitHub Milestones](https://github.com/ximaks00-hue/ic2port/milestones):

| Phase | Status | Focus |
|-------|--------|-------|
| Phase 1 — Immediate | ✅ Done | Module discharge · fusion export · EU cropnalyzer |
| Phase 2 — Survival polish | ✅ Done | Reactor components · crops · electric tools |
| Phase 3 — MV–HV infra | ✅ Done | Fluid cells · ESU · miner/pump |
| Phase 4 — Endgame | ✅ Done | Circuits · processing chain · teleporter/terraformer/replicator |
| Phase 5 — Release | 🔲 Planned | Balance pass · performance · CI polish · docs |

---

## Contributing

1. Check [open issues](https://github.com/ximaks00-hue/ic2port/issues) or [milestones](https://github.com/ximaks00-hue/ic2port/milestones)
2. Fork → branch → `./gradlew build` must pass
3. Conventional commits: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`
4. Update `en_us.json` + `ru_ru.json` for user-facing strings
5. Open a pull request with a clear description

---

## License

All Rights Reserved.

IC2 Port is **not affiliated** with IndustrialCraft² or IC2 Classic.  
Third-party textures: see [`CREDITS.md`](CREDITS.md).
