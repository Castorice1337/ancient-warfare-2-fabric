# AGENTS.md

## Project Focus

Columbina is a behavior-first high-version port of Ancient Warfare 2. The source of truth is not the old README and not memory; it is the combination of:

1. The local runnable 1.12.2 AW2 reference environment
2. `AncientWarfare2-reference`
3. The planning artifacts in `.planning/`

## Read Order Before Working

1. `.planning/PROJECT.md`
2. `.planning/REQUIREMENTS.md`
3. `.planning/ROADMAP.md`
4. `.planning/STATE.md`
5. Relevant files inside `.planning/research/`
6. Relevant legacy hotspots inside `AncientWarfare2-reference`

## Non-Negotiables

- Old AW2 behavior is the specification
- Milestone 1 is archaeology, not gameplay implementation
- Multiplayer sync is part of parity, not post-processing
- Save / quit / reload correctness is part of parity, not polish
- Do not port legacy classes blindly; port the documented behavior

## Current Roadmap Shape

- Milestone 1: Legacy Behavior Archaeology
  - Phase 1: Legacy Reference Bring-Up
  - Phase 2: Golden Scenario Capture
  - Phase 3: Behavior Spec And Source Mapping
- Milestone 2: High-Version Port
  - Phase 4: Modern Port Foundations
  - Phase 5: Logistics And Labor Vertical Slice
  - Phase 6: Combat, Structures, And Factions Slice
  - Phase 7: Multiplayer, Persistence, And Regression

## Implementation Guidance

- Every feature port should cite the matching Golden scenario and behavior spec section
- Prefer explicit server-authoritative state, explicit sync payloads, and explicit persistence boundaries
- When touching research, warehouse, courier, builder, commander, faction, or structure systems, inspect the corresponding legacy hotspot first
- New feature ideas that are outside the current roadmap should be captured separately, not silently folded into active scope

## Immediate Next Step

Use `$gsd-discuss-phase 1` or `$gsd-plan-phase 1` to start Milestone 1 work.
