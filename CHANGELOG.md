# Changelog

All notable changes to IC2 Port are documented here.

Format follows [Keep a Changelog](https://keepachangelog.com/).

## [0.1.0-SNAPSHOT] — 2026-06-22

### Added

#### Energy & infrastructure
- Custom EU network (LV → EV) with cables, transformers, BatBox / MFE / MFSU / **ESU**
- Generators: solid fuel, geothermal, solar (LV + **advanced MV**), wind, water
- **Miner** (HV quarry) and **Pump** (MV fluid drain)

#### Machines (20+)
- Core: macerator, extractor, compressor, recycler, electric/induction furnace, metal former
- Advanced: thermal centrifuge, mass fabricator, canner, vacuum canner
- Endgame: **electrolyzer**, **ore washer**, **alloy smelter**, **pattern replicator**

#### Nuclear & fusion
- Nuclear reactor with chambers, heat simulation, meltdown
- Reactor components: fuel rods, MOX, vents, exchangers, coolant cells, **neutron reflectors**, **plating**, **condensators**, **dual/quad rods**
- Fusion reactor (5×5×5 shell), valve export, comparator modes, auto-export toggle

#### Crops & brewing
- 18+ IC2-style crops with cross-breeding
- Crop sticks, harvester, cropmatron, crop analyzer (block + **EU handheld cropnalyzer**)
- Brewing barrel: beer, rum, whisky, potions

#### Armor & tools
- Nano / quantum suits with 6 armor modules (jetpack, energy shield, auto feeder, etc.)
- **Module discharge** into BatBox / MFE / MFSU
- Electric tools: drills, chainsaw, **electric wrench**, tree taps, scanners, EU reader

#### Fluids & materials
- **Universal fluid cells** with canner fill/empty
- **Electronic / advanced / complex circuits**
- UU-matter, iridium, construction foam, reinforced blocks

#### Endgame
- **Teleporter** + frequency transmitter
- **Terraformer** with cultivation / irrigation / desertification blueprints

#### Other
- 8 advancement milestones (rubber → fusion reactor)
- Worldgen: tin, uranium, rubber trees
- Localization: English + Russian
- 240+ JSON recipes, JEI integration in dev

### Changed
- README rewritten for current feature set
- GitHub Actions CI pipeline added
