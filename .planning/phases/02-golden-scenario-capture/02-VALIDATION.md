---
phase: 02
slug: golden-scenario-capture
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-04-11
---

# Phase 02 - Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | manual scenario capture plus markdown completeness checks |
| **Config file** | none |
| **Quick run command** | `Get-Content AncientWarfare2-reference\\run\\logs\\latest.log -Tail 200` |
| **Full suite command** | `Get-Content golden_test_cases.md` plus plan artifact checks |
| **Estimated runtime** | ~60-180 seconds per check |

## Sampling Rate

- **After every task commit:** run the relevant quick log or doc check
- **After every plan wave:** review the updated `golden_test_cases.md` sections
- **Before verification:** confirm all 12 required scenarios exist in the canonical doc
- **Max feedback latency:** one scenario batch

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 02-01-01 | 01 | 1 | ARCH-02 | doc | `Get-Content golden_test_cases.md` | W0 | pending |
| 02-02-02 | 02 | 2 | ARCH-02 | manual | evidence plus doc review | pending | pending |
| 02-03-02 | 03 | 3 | ARCH-02 | manual | evidence plus doc review | pending | pending |
| 02-04-02 | 04 | 4 | ARCH-02 | manual | evidence plus doc review | pending | pending |

## Wave 0 Requirements

- [ ] `golden_test_cases.md` exists
- [ ] `.planning/phases/02-golden-scenario-capture/02-EVIDENCE-INDEX.md` exists

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| In-game scenario execution | ARCH-02 | requires direct gameplay interaction | run the prepared batch and record observations immediately |
| Multiplayer synchronization | ARCH-02 | requires paired endpoints or equivalent host/client setup | validate both observed state and logs |
| Save / quit / reload retention | ARCH-02 | requires save-cycle interaction | exit and reload the target save, then compare states |

## Validation Sign-Off

- [ ] All plans update `golden_test_cases.md`
- [ ] Every scenario entry contains setup, steps, observations, expected results, and evidence
- [ ] No target scenario is silently omitted
- [ ] Known blockers are recorded explicitly
- [ ] `nyquist_compliant: true` can be set after plan execution

**Approval:** pending
