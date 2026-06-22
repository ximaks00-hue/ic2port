# Industrial Craft 2 — Official Reference Links

Official IC2 for modern Minecraft versions is distributed via Jenkins, not as public source.

## Latest builds (as of project setup)

- Jenkins 1.19+ branch: http://jenkins.ic2.player.to/job/IC2/job/1.19/
- Maven coordinates: `net.industrial-craft:industrialcraft-2:<version>`

## API dependency (Gradle example)

```gradle
repositories {
    maven {
        name = "ic2"
        url = "http://maven.ic2.player.to/"
    }
}

dependencies {
    compileOnly 'net.industrial-craft:industrialcraft-2:2.9.40-experimental:api'
}
```

## Key IC2 API packages (legacy 1.12 reference)

When inspecting decompiled JARs, look for:

- `ic2.api.energy` — EU network (IEnergySink, IEnergySource, IEnergyConductor)
- `ic2.api.energy.tile` — Tile-based energy interfaces
- `ic2.api.recipe` — Machine recipes (IRecipeInput, IMachineRecipe)
- `ic2.api.item` — Electric items (IElectricItem)
- `ic2.core` — Internal implementation (closed source)

## Mapping to IC2 Port API

| IC2 (legacy) | IC2 Port |
|--------------|----------|
| `IEnergySink` | `IEnergyAcceptor` |
| `IEnergySource` / `IEnergyEmitter` | `IEnergyEmitter` |
| `IEnergyConductor` | `IEnergyConductor` |
| `IEnergyTile` | `IEnergyNode` |
