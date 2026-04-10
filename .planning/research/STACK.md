# Stack Research: Columbina

## Framing

This project is not choosing a greenfield app stack from scratch. It is porting a behavior-rich Forge 1.12.2 mod to a modern Fabric 1.21.11 runtime. The right stack is therefore the one that minimizes drift between:

1. The local legacy reference environment
2. The current target repository
3. The systems that most strongly affect parity: persistence, networking, entity AI, inventories, templates, and regression testing

## Recommended Stack

| Layer | Recommendation | Rationale | Confidence |
|-------|----------------|-----------|------------|
| Legacy reference runtime | Minecraft `1.12.2` + Forge `14.23.5.2816` + AW2 `2.7.0` + Java `8` | This is the exact behavioral anchor used to define parity | High |
| Target mod runtime | Minecraft `1.21.11` + Fabric Loader `0.18.6` + Fabric API `0.141.3+1.21.11` + Loom `1.16-SNAPSHOT` + Java `21` | Already pinned by the current repository and therefore the lowest-friction target path | High |
| Language strategy | Keep Kotlin available for new infrastructure, but do not force all gameplay code into Kotlin if Java interop is simpler | The repo is Kotlin-enabled, but parity work will likely touch patterns easier to express in either language | Medium |
| State persistence | Use explicit server-authoritative state objects backed by modern Fabric persistence primitives and narrowly scoped NBT / codec serializers | Old AW2 spreads state across `WorldSavedData`, tile NBT, entity NBT, orders, and capabilities | High |
| Networking / UI sync | Use explicit C2S / S2C payloads and screen-handler-backed state sync for research, orders, warehouse views, and command tools | The legacy project is container-heavy and sync bugs are first-order parity failures | High |
| Template / structure pipeline | Use a data-driven structure/template layer plus a dedicated builder executor instead of inlining build logic into blocks/items | Old AW2 structure placement, builder progress, and worldgen all depend on template semantics | High |
| Regression layer | Treat Golden Reference docs plus replayable manual / automated harnesses as part of the stack, not optional QA | The project must prove behavior parity, not merely compile | High |

## What Not To Use

- Direct line-by-line translation of Forge `Capability` patterns into Fabric code
- Client-authoritative NPC or inventory logic
- Multi-loader abstractions in v1
- “We will fix multiplayer/persistence later” implementation sequencing

## Practical Porting Implications

- `ResearchTracker`, `FactionTracker`, `StructureMap`, `TownMap`, and warehouse data show that global and per-world state need an explicit modern ownership model
- `TileResearchStation`, `TileWarehouse*`, `TradeOrder`, and `TileStructureBuilder` show that container-heavy systems need a clean split between persisted state, transient job state, and synced UI state
- `NpcAIPlayerOwnedCourier` and commander logic show that inventory, navigation, and order execution must remain server-side if parity is to hold in multiplayer

## Recommendation Summary

The repository's pinned Fabric stack is already the right destination stack. The real architectural decision is not "which framework," but "how explicitly we model state, sync, and replayable behavior." That is why archaeology and Golden Reference capture must happen before feature porting.
