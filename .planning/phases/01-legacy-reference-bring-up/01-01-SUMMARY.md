---
phase: 01-legacy-reference-bring-up
plan: 01
subsystem: infra
tags: [forgegradle, java8, minecraft-1.12.2, aw2, reference-runtime]
requires: []
provides:
  - runnable local AW2 1.12.2 reference client
  - documented legacy setup and rescue notes
  - phase-tracked evidence for future archaeology work
affects: [golden-reference, phase-2, archaeology]
tech-stack:
  added: [Cursemaven rescue coordinates]
  patterns: [minimal legacy compatibility patching, optional compat kept out of default runtime]
key-files:
  created:
    - .planning/phases/01-legacy-reference-bring-up/01-LEGACY-REFERENCE-SETUP.md
    - .planning/phases/01-legacy-reference-bring-up/01-01-PLAN.md
    - .planning/phases/01-legacy-reference-bring-up/01-01-SUMMARY.md
    - .planning/phases/01-legacy-reference-bring-up/01-VERIFICATION.md
  modified:
    - AncientWarfare2-reference/build.gradle
    - AncientWarfare2-reference/gradle.properties
    - AncientWarfare2-reference/src/main/java/net/shadowmage/ancientwarfare/core/manual/ManualContentRegistry.java
    - .planning/ROADMAP.md
    - .planning/STATE.md
    - .planning/REQUIREMENTS.md
key-decisions:
  - "Use Java 8 and the original Gradle 4.8 wrapper instead of modernizing the legacy toolchain."
  - "Treat Wizardry, InfinityLib, and AgriCraft as compile-time compat dependencies for local boot rescue."
  - "Accept missing sound assets and template-load warnings as non-blocking for Phase 1."
patterns-established:
  - "Rescue the legacy reference environment with the smallest possible local compatibility patch."
  - "Document runnable reference evidence before attempting Golden Reference capture."
requirements-completed: [ARCH-01]
duration: ~3h
completed: 2026-04-10
---

# Phase 1 Plan 01: Legacy Reference Bring-Up Summary

**AW2 1.12.2 now boots locally, enters an integrated world, and has a documented recovery path for the rescued legacy toolchain.**

## Performance

- **Duration:** ~3h
- **Started:** 2026-04-10T14:00:00Z
- **Completed:** 2026-04-10T15:05:00Z
- **Tasks:** 3
- **Files modified:** 6

## Accomplishments
- Restored the legacy build bootstrap by rescuing unreachable dependency sources.
- Got the rescued reference client to load AW2 core mods and enter a local world.
- Captured reproducible setup instructions, runtime evidence, and caveats for future archaeology work.

## Task Commits

No phase-scoped git commits were created during this direct execution pass. The workspace changes remain local and reviewable.

## Files Created/Modified
- `.planning/phases/01-legacy-reference-bring-up/01-LEGACY-REFERENCE-SETUP.md` - reproducible reference setup notes
- `.planning/phases/01-legacy-reference-bring-up/01-01-PLAN.md` - minimal GSD plan placeholder for this executed work
- `.planning/phases/01-legacy-reference-bring-up/01-VERIFICATION.md` - goal-backward verification record
- `AncientWarfare2-reference/build.gradle` - reachable repositories and rescued dependency coordinates
- `AncientWarfare2-reference/gradle.properties` - Cursemaven project/file IDs for rescued legacy dependencies
- `AncientWarfare2-reference/src/main/java/net/shadowmage/ancientwarfare/core/manual/ManualContentRegistry.java` - null-safe language fallback during client startup

## Decisions Made

- Kept the legacy toolchain on Java 8 and Gradle 4.8 instead of performing a larger modernization.
- Shifted optional compat mods out of the default local runtime path to keep Phase 1 focused on bring-up, not full compat parity.
- Treated missing sound assets and several structure-template warnings as acceptable non-blockers for this phase because boot, world join, and shutdown all succeeded.

## Deviations from Plan

This plan was written after the direct execution to provide GSD traceability. The implementation itself was already completed in-session before the placeholder plan was backfilled.

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Replaced dead dependency routes with reachable Cursemaven coordinates**
- **Found during:** legacy build bootstrap
- **Issue:** the original dependency chain no longer resolved reliably from the historical upstream repositories
- **Fix:** added Cursemaven-backed coordinates and pinned project/file IDs for the required artifacts
- **Files modified:** `AncientWarfare2-reference/build.gradle`, `AncientWarfare2-reference/gradle.properties`
- **Verification:** `gradlew.bat tasks --all` succeeded under Java 8

**2. [Rule 3 - Blocking] Removed crashing optional compat mods from the default local runtime**
- **Found during:** first rescued client boot
- **Issue:** optional compatibility mods blocked the local reference client before AW2 itself could finish loading
- **Fix:** changed those dependencies to compile-time only for the local rescue path
- **Files modified:** `AncientWarfare2-reference/build.gradle`
- **Verification:** the rescued client later loaded 13 mods successfully and joined a world

**3. [Rule 1 - Bug] Guarded manual startup against null language initialization**
- **Found during:** second rescued client boot
- **Issue:** `ManualContentRegistry` assumed a non-null current language and crashed during startup
- **Fix:** added an `en_us` fallback when the language manager had not fully initialized
- **Files modified:** `AncientWarfare2-reference/src/main/java/net/shadowmage/ancientwarfare/core/manual/ManualContentRegistry.java`
- **Verification:** the subsequent client run reached mod-loaded, world-join, and clean shutdown states

---

**Total deviations:** 3 auto-fixed (2 blocking, 1 bug)
**Impact on plan:** all deviations were necessary to make the legacy reference environment runnable; no scope creep beyond Phase 1.

## Issues Encountered

- ForgeGradle's asset downloader still receives many `400` responses from old Mojang HTTP asset URLs, so the reference environment logs missing sound events.
- Some structure templates log load errors during startup, but they do not block the client from reaching gameplay.
- Development-environment signature warnings still appear for some deobfuscated dependency jars.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- The local reference client is now runnable and documented.
- Phase 2 can begin recording Golden Reference scenarios from this rescued environment.
- Multiplayer and asset completeness checks are still open, but they no longer block archaeology work.

---
*Phase: 01-legacy-reference-bring-up*
*Completed: 2026-04-10*
