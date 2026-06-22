# IC2 JAR artifacts (local reference)

Downloaded from http://maven.ic2.player.to/

| File | Version | Purpose |
|------|---------|---------|
| `industrialcraft-2-2.8.222-ex112-api.jar` | IC2 1.12.2 | Public API interfaces (`ic2.api.*`) |
| `industrialcraft-2-2.8.222-ex112-dev.jar` | IC2 1.12.2 | Deobfuscated dev jar for IDE inspection |

## Inspecting the API

Open the `-api.jar` in your IDE or extract with:

```bash
jar tf industrialcraft-2-2.8.222-ex112-api.jar | findstr ic2/api/energy
```

Key packages:

- `ic2.api.energy.tile.IEnergySink` — energy consumer
- `ic2.api.energy.tile.IEnergySource` — energy producer
- `ic2.api.energy.tile.IEnergyConductor` — cable
- `ic2.api.recipe.*` — machine recipes
