---
phase: 01-legacy-reference-bring-up
verified: 2026-04-10T15:05:00Z
status: passed
score: 3/3 must-haves verified
---

# Phase 1: Legacy Reference Bring-Up Verification Report

**Phase Goal:** Bring the local AW2 1.12.2 reference environment into a reproducible working state.
**Verified:** 2026-04-10T15:05:00Z
**Status:** passed

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | The local machine can bootstrap the AW2 1.12.2 reference workspace with Java 8 and the legacy Gradle wrapper. | VERIFIED | `gradlew.bat tasks --all` and `gradlew.bat setupDecompWorkspace` completed successfully with `JAVA_HOME=C:\Program Files\Java\jdk1.8.0_202` |
| 2 | `gradlew runClient` reaches an in-world playable state with core Ancient Warfare mods loaded. | VERIFIED | `run/logs/latest.log` contains `Forge Mod Loader has successfully loaded 13 mods`, `Player34 joined the game`, and orderly save/shutdown lines |
| 3 | Reproducible setup instructions and known caveats are documented for later archaeology work. | VERIFIED | `.planning/phases/01-legacy-reference-bring-up/01-LEGACY-REFERENCE-SETUP.md` records commands, local rescue patches, evidence, and caveats |

**Score:** 3/3 truths verified

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `AncientWarfare2-reference/build.gradle` | Reachable rescued dependency config | EXISTS + SUBSTANTIVE | Contains Cursemaven repository and rescued dependency coordinates |
| `AncientWarfare2-reference/gradle.properties` | Rescued Cursemaven IDs | EXISTS + SUBSTANTIVE | Contains `ftblib_cf_fileid`, `agricraft_cf_projectid`, `infinitylib_cf_projectid`, `eb_cf_projectid` |
| `AncientWarfare2-reference/src/main/java/net/shadowmage/ancientwarfare/core/manual/ManualContentRegistry.java` | Null-safe manual startup path | EXISTS + SUBSTANTIVE | Contains explicit fallback when current language is null |
| `.planning/phases/01-legacy-reference-bring-up/01-LEGACY-REFERENCE-SETUP.md` | Reproducible setup note | EXISTS + SUBSTANTIVE | Documents commands, evidence, and caveats |

**Artifacts:** 4/4 verified

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| `build.gradle` | `gradle.properties` | Cursemaven project/file properties | WIRED | dependency strings read the pinned project/file IDs defined in `gradle.properties` |
| `ManualContentRegistry.java` | client startup | null-safe language fallback | WIRED | startup no longer crashes on missing current language |
| setup document | `run/logs/latest.log` | explicit evidence references | WIRED | setup note cites boot, world join, and shutdown proof from the log |

**Wiring:** 3/3 connections verified

## Requirements Coverage

| Requirement | Status | Blocking Issue |
|-------------|--------|----------------|
| ARCH-01: local AW2 1.12.2 reference environment can be started on this machine | SATISFIED | - |

**Coverage:** 1/1 requirements satisfied

## Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| `run/logs/latest.log` | runtime | missing sound assets from legacy HTTP asset endpoint | Warning | Noisy logs and incomplete audio, but local archaeology still works |
| `run/logs/latest.log` | runtime | several structure template load errors | Warning | May affect some later world-gen validation, but Phase 1 goal still passes |

**Anti-patterns:** 2 found (0 blockers, 2 warnings)

## Human Verification Required

None - the phase goal is satisfiable through build and runtime evidence already present in the workspace.

## Gaps Summary

**No critical gaps found.** Phase goal achieved. Ready to proceed.

## Verification Metadata

**Verification approach:** goal-backward
**Must-haves source:** `01-01-PLAN.md`
**Automated checks:** 3 passed, 0 failed
**Human checks required:** 0
**Total verification time:** ~10 min

---
*Verified: 2026-04-10T15:05:00Z*
*Verifier: the agent*
