# Feature Research: Columbina

## Table Stakes

These are the capabilities the project must ship, because they define the user-facing identity of an AW2 high-version port.

| Category | Feature | Why It Is Table Stakes | Complexity | Dependencies |
|----------|---------|------------------------|------------|--------------|
| Reference archaeology | Runnable 1.12.2 reference environment | Without this, parity claims are guesswork | Medium | Legacy setup, pinned dependencies |
| Reference archaeology | Golden scenario catalog for the 12 required flows | This is the project's executable spec | High | Reference runtime, capture format |
| Progression | Research tree unlock loop | Old AW2 gates many systems through research | High | Persistence, UI sync |
| Logistics | Warehouse creation and expansion | Central inventory authority is core AW2 identity | High | Persistence, sync, item routing |
| Logistics | Courier two-point transfer | This is the first real NPC logistics loop | High | Inventory access, navigation, orders |
| Labor | Builder template construction | Core player fantasy: plan structures and watch them get built | High | Templates, progress persistence, inventory sourcing |
| Labor | At least two worker role full loops | Worker automation is a signature AW2 system | High | Job system, inventories, routing |
| Combat | Soldier recruitment and equipment | Needed to make combat and command meaningful | Medium | Ownership, inventories |
| Combat | Commander tool select / move / patrol / attack | This is the player control surface for NPC combat | High | Targeting, AI orders, sync |
| Combat | One siege engine build-and-attack loop | Required for a credible first combat parity slice | Medium | Structures, combat, persistence |
| World/factions | One hostile structure encounter and cleanup loop | AW2 is not only player-owned NPCs; world hostility matters | High | Worldgen/spawn logic, combat |
| World/factions | One friendly or neutral faction interaction | Factions are part of the AW2 world contract | Medium | Standing/trade logic |
| Quality | Multiplayer sync and save/load parity | User explicitly listed both as must-cover Golden cases | High | Every other system |

## Differentiators

These are not new gameplay features for players first. They differentiate the port project itself by making parity sustainable.

| Differentiator | Why It Matters | Complexity |
|----------------|----------------|------------|
| Behavior-first specs (`feature_inventory.md`, `behavior_spec.md`, `golden_test_cases.md`) | Makes future implementation work falsifiable instead of subjective | Medium |
| Golden regression harness for target implementation | Prevents silent drift while porting additional systems | High |
| Explicit modern state boundaries for sync/persistence | Avoids recreating legacy desync bugs in a new loader/runtime | High |
| Traceability from legacy classes to new systems | Reduces “where did this behavior come from?” debugging time | Medium |

## Anti-Features

These are tempting directions that should be actively resisted during v1.

| Anti-Feature | Why To Avoid It |
|--------------|-----------------|
| Porting whole AW2 modules before the 12 required scenarios are understood | Scope explodes before parity foundations exist |
| Rewriting old code structure one-for-one | Preserves old architecture without preserving the reasons behind behavior |
| Shipping singleplayer-only and “fixing multiplayer later” | Sync is part of the spec, not post-launch polish |
| Adding brand-new mechanics before parity | Makes regressions impossible to reason about |
| Supporting multiple loaders in the first pass | Multiplies complexity before the Fabric baseline is stable |

## Recommended Scope Boundary

The first credible version of this project is not “all of AW2 on 1.21.11.” It is:

- A reproducible legacy spec
- A documented set of golden behaviors
- A modern runtime that reproduces the chosen core loops with multiplayer and persistence intact
