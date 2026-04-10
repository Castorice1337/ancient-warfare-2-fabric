---
phase: 02-golden-scenario-capture
plan: 01
subsystem: testing
tags: [golden-reference, archaeology, documentation, aw2]
requires:
  - phase: 01-legacy-reference-bring-up
    provides: runnable local AW2 1.12.2 reference environment and startup evidence
provides:
  - canonical golden scenario workbook
  - evidence index for runtime and save artifacts
  - batch note skeletons for phased capture
affects: [phase-02-batch-a, phase-02-batch-b, phase-02-batch-c, phase-03]
tech-stack:
  added: []
  patterns: [canonical workbook first, evidence index before capture, batch-by-setup reuse]
key-files:
  created:
    - golden_test_cases.md
    - .planning/phases/02-golden-scenario-capture/02-EVIDENCE-INDEX.md
    - .planning/phases/02-golden-scenario-capture/02-BATCH-A-NOTES.md
    - .planning/phases/02-golden-scenario-capture/02-BATCH-B-NOTES.md
    - .planning/phases/02-golden-scenario-capture/02-BATCH-C-NOTES.md
    - .planning/phases/02-golden-scenario-capture/02-01-SUMMARY.md
  modified:
    - .planning/STATE.md
key-decisions:
  - "Seed the 12 required scenarios into one canonical workbook before any in-game capture begins."
  - "Use batch note files to isolate raw observations before consolidating into the canonical doc."
patterns-established:
  - "Golden capture is batch-driven and evidence-backed."
  - "Scenario blockers must be written explicitly instead of leaving empty placeholders."
requirements-completed: []
duration: ~20min
completed: 2026-04-11
---

# Phase 2 Plan 01: Golden Scenario Capture Summary

**Phase 2 now has a canonical 12-scenario Golden workbook, a shared evidence index, and three batch capture work areas ready for execution.**

## Performance

- **Duration:** ~20 min
- **Started:** 2026-04-11T00:20:00Z
- **Completed:** 2026-04-11T00:40:00Z
- **Tasks:** 2
- **Files modified:** 6

## Accomplishments
- Created `golden_test_cases.md` with all required scenario headings and a stable capture schema.
- Added a shared evidence index pointing to the working log/save anchors from the rescued reference runtime.
- Created Batch A / B / C note skeletons so later capture work can happen in organized passes.

## Task Commits

No phase-scoped git commits were created during this execution pass. The workspace changes remain local and reviewable.

## Files Created/Modified
- `golden_test_cases.md` - canonical workbook for all required Golden scenarios
- `.planning/phases/02-golden-scenario-capture/02-EVIDENCE-INDEX.md` - evidence lookup for logs, saves, and screenshots
- `.planning/phases/02-golden-scenario-capture/02-BATCH-A-NOTES.md` - singleplayer economy/labor/control batch notes
- `.planning/phases/02-golden-scenario-capture/02-BATCH-B-NOTES.md` - combat/world/faction batch notes
- `.planning/phases/02-golden-scenario-capture/02-BATCH-C-NOTES.md` - multiplayer/persistence batch notes
- `.planning/STATE.md` - Phase 2 status moved forward from ready to planned/in progress context

## Decisions Made

- Keep one canonical workbook at repo root rather than splitting into twelve separate files.
- Record raw capture notes in batch files before consolidating them into the canonical workbook.
- Treat runtime log and save artifacts as first-class evidence, not just supplemental notes.

## Deviations from Plan

None - Wave 1 executed exactly as planned.

## Issues Encountered

- No blocking issues during workbook/bootstrap creation.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Batch A can start immediately.
- The first checkpoint now depends on in-game capture rather than more planning/doc structure.

---
*Phase: 02-golden-scenario-capture*
*Completed: 2026-04-11*
