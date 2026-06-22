# IC2 Port

Industrial Craft 2 reimplementation skeleton for **Minecraft 1.20.1** / **Forge 47.x** / **Java 17**.

## Project structure

```
src/main/java/dev/ic2port/
├── IC2PortMod.java          # Main mod class
├── Reference.java           # MOD_ID, MOD_NAME, VERSION
├── api/energy/              # EU energy network interfaces
├── block/                   # Blocks (future)
├── item/                    # Items (future)
├── blockentity/             # Tile entities / machines (future)
├── menu/                    # GUI containers (future)
├── network/                 # Packets (future)
├── recipe/                  # Custom recipe types (future)
├── setup/                   # DeferredRegister managers
└── datagen/                 # Data generators
```

## Build

```bash
./gradlew build
```

Run client:

```bash
./gradlew runClient
```

Run data generators:

```bash
./gradlew runData
```

## Reference material

See [`reference/README.md`](reference/README.md) for IC2 comparison artifacts and documentation links.

## License

All Rights Reserved.
