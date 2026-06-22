# IC2 JAR artifacts (local reference)

Not committed to git (see root `.gitignore`). Download with:

```bash
.\gradlew.bat downloadIc2ReferenceJars
```

| File | Version | Purpose |
|------|---------|---------|
| `industrialcraft-2-2.8.222-ex112-api.jar` | IC2 1.12.2 | `compileOnly` in Gradle — public API (`ic2.api.*`) |
| `industrialcraft-2-2.8.222-ex112-dev.jar` | IC2 1.12.2 | Manual IntelliJ library — decompiled implementation lookup |

Setup guide: [`reference/docs/IC2_REFERENCE_SETUP.md`](../../docs/IC2_REFERENCE_SETUP.md)

## Quick API package list

```bash
jar tf industrialcraft-2-2.8.222-ex112-api.jar | findstr ic2/api/energy
```

- `ic2.api.energy.tile.IEnergySink` — energy consumer
- `ic2.api.energy.tile.IEnergySource` — energy producer
- `ic2.api.energy.tile.IEnergyConductor` — cable
- `ic2.api.recipe.*` — machine recipes
