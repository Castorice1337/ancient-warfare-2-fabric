# Legacy Reference Setup

## Goal

Bring the local AW2 1.12.2 reference environment into a reproducible working state for later archaeology work.

## Workspace

- Reference repo: `D:\1.21.11\AncientWarfare2-reference`
- Target runtime: Minecraft `1.12.2`
- Forge: `14.23.5.2816`
- AW2 version anchor: `2.7.0`
- Required JDK: Java 8 (`C:\Program Files\Java\jdk1.8.0_202`)

## Repro Commands

Run all commands from `D:\1.21.11\AncientWarfare2-reference`.

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk1.8.0_202'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat tasks --all
.\gradlew.bat setupDecompWorkspace
.\gradlew.bat runClient
```

## Local Rescue Patches Applied

### 1. Dependency source rescue

The legacy build could no longer resolve all dependencies from its original upstreams. Local fixes were applied to:

- route `FTBLib` through reachable Cursemaven coordinates
- add explicit Cursemaven project/file IDs to `gradle.properties`
- make `Electroblob's Wizardry`, `InfinityLib`, and `AgriCraft` available for compilation without forcing them into the default local runtime

Files touched:

- `AncientWarfare2-reference/build.gradle`
- `AncientWarfare2-reference/gradle.properties`

### 2. Manual boot compatibility fix

`ManualContentRegistry` assumed a non-null current language during startup. In this rescued environment that assumption was false during initialization, so a null-safe fallback to `en_us` was added.

File touched:

- `AncientWarfare2-reference/src/main/java/net/shadowmage/ancientwarfare/core/manual/ManualContentRegistry.java`

## Proof Of Success

Primary evidence:

- `AncientWarfare2-reference/run/logs/latest.log`

Observed success markers from the successful run:

- `Forge Mod Loader has successfully loaded 13 mods`
- integrated server started for `New World`
- player `Player34` joined the local world
- normal shutdown sequence completed with world save

Relevant log milestones from the successful run:

- `2026-04-10 22:49:52` - client launch begins
- `2026-04-10 22:50:35` - mods loaded successfully
- `2026-04-10 22:58:52` - player joined integrated server world
- `2026-04-10 23:00:11` - orderly client/server shutdown begins

## Known Caveats

These do not currently block Phase 1 success:

- ForgeGradle's legacy asset downloader still hits many `http://resources.download.minecraft.net/...` endpoints that now return `400`, so many sounds are missing in the local reference environment.
- Some structure templates log load errors during startup.
- Signature warnings appear for deobfuscated dependency jars in the development environment.

## Interpretation

Phase 1's core requirement is satisfied once the rescued local environment can boot, enter a world, and be rerun with documented steps. Missing sound assets and non-fatal template warnings should be tracked, but they do not block moving into Golden Reference recording.
