# State: Columbina

**Initialized:** 2026-04-10
**Last updated:** 2026-04-10 after Phase 1 execution

## Project Reference

See: `.planning/PROJECT.md` (updated 2026-04-10)

**Core value:** Players should experience the AW2 core gameplay loop on Minecraft 1.21.11 with behavior parity that can be verified against the local 1.12.2 reference.
**Current focus:** Phase 2 - Golden Scenario Capture

## Current Milestone

**Milestone 1: Legacy Behavior Archaeology**

Primary outputs:
- `feature_inventory.md`
- `behavior_spec.md`
- `golden_test_cases.md`

## Phase Status

| Phase | Name | Status |
|-------|------|--------|
| 1 | Legacy Reference Bring-Up | Complete |
| 2 | Golden Scenario Capture | Ready |
| 3 | Behavior Spec And Source Mapping | Pending |
| 4 | Modern Port Foundations | Pending |
| 5 | Logistics And Labor Vertical Slice | Pending |
| 6 | Combat, Structures, And Factions Slice | Pending |
| 7 | Multiplayer, Persistence, And Regression | Pending |

## Current Working Set

- Target repo: Fabric `1.21.11` / Java `21` / `columbina`
- Legacy reference repo: AW2 `2.7.0` / Minecraft `1.12.2` / Forge `14.23.5.2816`
- Phase 1 proof: local reference client booted, joined an integrated world, and shut down cleanly
- Next execution target: record the first Golden Reference scenarios on top of the rescued local environment

## Open Risks

- ForgeGradle still fails to download many legacy sound assets from old Mojang HTTP endpoints, so the reference environment has missing sound warnings
- Some structure templates report load errors during startup and may affect later scenario coverage
- Multiplayer and dedicated server validation still need to be exercised explicitly in later archaeology work
- The reference repo contains local rescue patches that should be documented carefully before Phase 2 broadens scope

## Suggested Next Commands

- `$gsd-execute-phase 2`
- `$gsd-discuss-phase 2`
- `$gsd-plan-phase 2`

## Notes

- Phase 1 artifacts live in `.planning/phases/01-legacy-reference-bring-up/`
- The rescued reference setup is documented in `01-LEGACY-REFERENCE-SETUP.md`
- ARCH-01 is now treated as complete for project tracking
