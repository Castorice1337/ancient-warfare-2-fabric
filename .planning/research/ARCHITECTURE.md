# Architecture Research: Columbina

## Recommended Component Boundaries

| Component | Responsibility | Legacy Hotspots |
|-----------|----------------|-----------------|
| `legacy-reference-runtime` | Scripts, docs, saves, and instructions for launching AW2 1.12.2 locally | Legacy Gradle/Forge setup, runtime configs |
| `golden-knowledge-base` | Stores feature inventory, behavior spec, golden test cases, and evidence links | All 12 user-selected scenarios |
| `progression-core` | Research data, unlock state, research station flow, recipe gating | `ResearchTracker`, `TileResearchStation`, research GUI/container classes |
| `logistics-core` | Warehouse authority, storage expansion, stock visibility, courier routing, transfer rules | `TileWarehouse*`, warehouse containers, stock viewers, courier AI |
| `npc-job-system` | Worker roles, task execution, job transitions, ownership and orders | Worker entities, role subtypes, player-owned AI classes |
| `structure-template-system` | Template loading, previewing, build progress, structure generation, builder behavior | `TileStructureBuilder`, `StructureBuilder*`, template/worldgen packages |
| `combat-command-system` | Soldier recruitment, equipment, command tool actions, siege behavior | Commander AI, soldier entities, siege engineer / vehicle helpers |
| `faction-world-system` | Hostile structure interactions, faction standing, neutral/friendly interactions | `FactionTracker`, faction entities, `SpawnerSettings`, territory/town data |
| `sync-persistence-layer` | Shared serialization, persistence ownership, and explicit network sync boundaries | `WorldSavedData`, entity/tile NBT, container sync points |
| `regression-layer` | Replays target scenarios, records pass/fail deltas, links failures back to docs | Golden cases, target implementation harness |

## Data Flow

### Player-facing runtime flow

1. Player action enters through an item, block interaction, or UI
2. Server-side system interprets the action into an order, state mutation, or build request
3. Domain state changes in the relevant subsystem (`progression`, `logistics`, `npc-jobs`, `structures`, `factions`)
4. State is persisted and selectively synced to clients
5. UI and rendering consume synced snapshots, not authority

### Porting feedback flow

1. Legacy reference scenario is executed
2. Behavior is captured into Golden docs
3. Target implementation is built against that documented behavior
4. Regression run compares target result against Golden expectation
5. Any mismatch is classified as bug, missing feature, or intentional deviation

## Suggested Build Order

1. Legacy reference runtime
2. Golden knowledge base
3. Progression core
4. Shared sync/persistence layer
5. Logistics core
6. NPC job system
7. Structure/template system
8. Combat/command system
9. Faction/world system
10. Regression layer hardening

## Why This Order

- The first two components define the spec
- `progression-core` is a good first implementation slice because it touches UI, state, and persistence without requiring all combat/world systems
- `logistics-core`, `npc-job-system`, and `structure-template-system` are tightly coupled and should share state conventions before large-scale implementation
- `combat-command-system` and `faction-world-system` build on the same ownership, targeting, and persistence assumptions
- The regression layer must exist early conceptually, but becomes most powerful after at least one target slice is playable
