# IC2 Port Addon API (v1)

Third-party mods should depend on the **`api` classifier JAR** published alongside the main mod artifact:

```gradle
dependencies {
    compileOnly fg.deobf("dev.ic2port:ic2port:${ic2port_version}:api")
}
```

Build locally:

```bash
./gradlew apiJar
# output: build/libs/ic2port-<version>-api.jar
```

## Stable packages

| Package | Purpose |
|---------|---------|
| `dev.ic2port.api` | `IC2PortAPI` entry point |
| `dev.ic2port.api.energy` | EU nodes, tiers, `EnergyHelper` |
| `dev.ic2port.api.reactor` | Reactor component contracts |
| `dev.ic2port.api.crops` | Crop definitions, `CropRegisterEvent` |
| `dev.ic2port.api.recipes` | `IMachineRecipe`, `MachineRecipeRegistryEvent` |
| `dev.ic2port.api.events` | Gameplay hooks (`ScrapBoxEvent`, …) |
| `dev.ic2port.api.blocks` | Block behaviour (`IWrenchable`) |

Do **not** reference `dev.ic2port.setup`, `dev.ic2port.util`, or block entity implementations — those are internal.

## Registering crops

Listen on the Forge event bus:

```java
@SubscribeEvent
public void onCropRegister(CropRegisterEvent event) {
    event.getRegistry().register(myCrop);
}
```

Fired after built-in crops during `FMLCommonSetup`.

## Registering machine recipes

```java
@SubscribeEvent
public void onMachineRecipes(MachineRecipeRegistryEvent event) {
    event.getRegistry().register(myRecipe);
}
```

### Runtime bridge coverage (v1)

Addon machine recipes are mapped at runtime for:

- `macerator`
- `compressor`
- `extractor` and `centrifugal_extractor`
- `electric_furnace` and `induction_furnace`
- `electrolyzer`
- `ore_washer`
- `metal_former`
- `alloy_smelter`
- `thermal_centrifuge`

If you target another machine id, treat it as unsupported in v1 until explicitly documented here.

## EU helpers

```java
double remainder = EnergyHelper.injectIntoNeighbor(level, pos, Direction.NORTH, 32.0, EnergyTier.LV);
```

## Scrap box hook

Cancel or replace loot:

```java
@SubscribeEvent
public void onScrapBox(ScrapBoxEvent event) {
    event.setReward(customStack);
}
```

## Wrenchable blocks

Implement `IWrenchable` on your block class to customize dismantle drop chance and pre-remove logic.
