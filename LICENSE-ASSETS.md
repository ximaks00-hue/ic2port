# Asset Licensing Policy

This document defines which third-party texture sources are allowed in IC2 Port
release artifacts and which sources are reference-only.

## Tier model

### Tier Green (allowed in release JAR)

- **Tech Reborn** ([https://github.com/TechReborn/TechReborn](https://github.com/TechReborn/TechReborn))
  - License: MIT
  - Allowed: yes, with attribution in `CREDITS.md`
- **Modern Industrialization** ([https://github.com/AztechMC/Modern-Industrialization](https://github.com/AztechMC/Modern-Industrialization))
  - License: CC0 (assets)
  - Allowed: yes
- **LogicWorlds/IC2ClassicFaithful32** ([https://github.com/logixy/IC2ClassicFaithful32](https://github.com/logixy/IC2ClassicFaithful32))
  - License: Unlicense
  - Allowed: yes, but paths are older and must be validated per asset

### Tier Yellow (conditional)

- **CrossVas/F32AddonsIC2Classic** ([https://github.com/CrossVas/F32AddonsIC2Classic](https://github.com/CrossVas/F32AddonsIC2Classic))
  - Repository note: README says personal use and non-official pack
  - Allowed: only after explicit written permission from the author
  - Until permission exists, treat as reference-only for parity analysis

### Tier Red (not allowed for extraction/redistribution)

- **IC2 Classic game assets** (CurseForge project license: All Rights Reserved)
- **IC2 Experimental original assets** (All Rights Reserved)

These may be used as visual references by artists but must not be bundled in
the IC2 Port release JAR unless explicit permission is granted by rights holders.

## Release gate

Before publishing:

1. `docs/ASSET_MANIFEST.json` must list a source and license tier for each ID.
2. No Tier Red entries may be marked as bundled.
3. No Tier Yellow entries may be bundled without an approval record.
4. `CREDITS.md` must include all bundled third-party sources.

## Approval records

Store permissions in `docs/permissions/` as plain text snapshots:

- `crossvas-redistribution-approval.txt`
- `speiger-asset-permission.txt`

If no record exists, Tier Yellow stays external-only (optional resource pack).
