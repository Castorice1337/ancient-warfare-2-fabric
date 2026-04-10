# Research Summary: Columbina

## Stack

- Legacy reference anchor: AW2 `2.7.0` on Minecraft `1.12.2` / Forge `14.23.5.2816` / Java `8`
- Target implementation anchor: Fabric `1.21.11` / Loader `0.18.6` / Fabric API `0.141.3+1.21.11` / Java `21`
- The key architectural choice is not the loader anymore; it is how explicitly the project models authority, synchronization, persistence, and replayable behavior

## Table Stakes

- Runnable 1.12.2 reference environment
- Golden scenario capture for the 12 required flows
- Research progression parity
- Warehouse, courier, builder, and worker-loop parity
- Soldier, command, siege, hostile structure, and faction interaction parity
- Multiplayer sync and save/load parity

## Recommended Architecture

- Keep a clear boundary between legacy reference artifacts and modern implementation code
- Build the modern port around explicit server-authoritative subsystem boundaries
- Treat regression documentation as a first-class artifact, not a side task

## Build Order

1. Bring up the legacy runtime
2. Capture Golden scenarios
3. Write behavior specs and source maps
4. Establish modern foundations with research progression
5. Port logistics and labor
6. Port combat, structures, and factions
7. Close multiplayer, persistence, and Golden regression

## Watch Out For

- Sync bugs masquerading as UI bugs
- Save/load bugs discovered only after multiple systems are integrated
- Scope creep before the 12 Golden scenarios are stabilized
- Direct source translation that preserves technical shape but not gameplay behavior
