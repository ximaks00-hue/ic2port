# IC2 Reference Setup (Gradle + IntelliJ IDEA)

This guide explains how to use the official IC2 JARs as a **read-only reference** while developing IC2 Port. The IC2 API is on the compile classpath for navigation and comparison only — it is **not** shipped in the release mod.

## 1. Download reference JARs

After cloning the repo, run:

```bash
.\gradlew.bat downloadIc2ReferenceJars
```

This fetches into `reference/ic2/jars/`:

| File | Purpose |
|------|---------|
| `industrialcraft-2-2.8.222-ex112-api.jar` | Public API (`ic2.api.*`) — wired as `compileOnly` |
| `industrialcraft-2-2.8.222-ex112-dev.jar` | Full deobfuscated mod — IDE inspection / decompiler only |

JARs are gitignored (large binaries). Every developer runs the download task once.

If Maven is unreachable, download manually from http://maven.ic2.player.to/ and place files in `reference/ic2/jars/`.

## 2. Gradle sync

```bash
.\gradlew.bat --refresh-dependencies compileJava
```

In IntelliJ: **Gradle** tool window → **Reload All Gradle Projects**.

You should see under **External Libraries**:

- `industrialcraft-2-2.8.222-ex112-api.jar` (compile-only)

## 3. Attach dev JAR for implementation lookup (IntelliJ)

The **dev JAR must not** be added to `build.gradle` dependencies — it would pull the entire IC2 mod onto the classpath.

Instead, attach it manually for decompiled source browsing:

1. **File** → **Project Structure** (`Ctrl+Alt+Shift+S`)
2. **Libraries** → **+** → **Java**
3. Select `reference/ic2/jars/industrialcraft-2-2.8.222-ex112-dev.jar`
4. Name it `IC2 Reference (dev)` → **OK**
5. On the next dialog, choose **Cancel** (do not add to any module) — library stays at project level for navigation only

Alternatively, without adding a library:

1. Open **External Libraries** → expand the API jar
2. Open e.g. `ic2.api.energy.tile.IEnergySink`
3. Click **Choose Sources** / **Attach Sources** and point to the **dev JAR** (IntelliJ will decompile on the fly)

## 4. Compare with IC2 Port Energy API

Open side by side:

| IC2 (1.12 API) | IC2 Port (1.20.1) |
|----------------|-------------------|
| `ic2.api.energy.tile.IEnergySink` | `dev.ic2port.api.energy.IEnergyAcceptor` |
| `ic2.api.energy.tile.IEnergySource` | `dev.ic2port.api.energy.IEnergyEmitter` |
| `ic2.api.energy.tile.IEnergyConductor` | `dev.ic2port.api.energy.IEnergyConductor` |
| `ic2.api.energy.tile.IEnergyTile` | `dev.ic2port.api.energy.IEnergyNode` |

IC2 Classic (open API in `reference/ic2-classic/`) is also useful — especially `ic2.api.energy.tile.*` on branch `1.19.x`.

## 5. Rules

- **Do not** `import ic2.*` in production mod code until an explicit compatibility layer is designed.
- **Do not** copy IC2 assets (textures, sounds, models) without permission.
- IC2 reference targets **Minecraft 1.12.2** — use it for behaviour and API shape, not copy-paste into 1.20.1 Forge code.

## 6. Optional: IC2Classic API in IDE

The folder `reference/ic2-classic/` contains the published IC2Classic API sources. Mark it as **Sources** if you prefer readable Java over decompiled IC2 Experimental.
