# Phase 2: Golden Scenario Capture - Research

**Researched:** 2026-04-11
**Domain:** Legacy gameplay archaeology and Golden Reference capture for AW2 1.12.2
**Confidence:** HIGH

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- Use `AncientWarfare2-reference` as the only execution environment for Golden capture.
- Use the rescued Java 8 + Gradle 4.8 boot path from Phase 1 as the standard launch path.
- Use `golden_test_cases.md` as the evolving canonical capture document for this phase.
- Record each scenario with `setup / steps / observations / expected results / evidence / blockers`.
- Every scenario must point to at least one durable evidence source: text notes, screenshots, or log references.
- Separate singleplayer core loops from multiplayer/persistence checks.
- Phase 2 stops at scenario capture and scenario-level expected behavior.

### the agent's Discretion
- Exact scenario ordering inside each capture batch
- Whether to use one reusable world or multiple saves for different batches
- How to structure evidence index files under the phase directory

### Deferred Ideas (OUT OF SCOPE)
- Full system inventory and taxonomy output
- Input/state/output/boundary decomposition
- Full source-map coverage for every behavior

</user_constraints>

<research_summary>
## Summary

This phase does not need ecosystem web research; it needs disciplined local archaeology. The standard approach is to capture scenarios in batches that minimize setup churn, anchor every claim to at least one durable artifact, and separate runtime capture from later interpretation.

The best structure for this project is to create a scenario workbook and evidence schema first, capture the singleplayer-heavy scenarios in grouped batches, reserve multiplayer and save/reload cases for a dedicated final batch, and treat `golden_test_cases.md` as a living canonical artifact that is filled progressively rather than written only at the end.

**Primary recommendation:** Plan Phase 2 around one setup plan plus three capture batches, with every capture batch ending in immediate documentation updates to `golden_test_cases.md`.
</research_summary>

<standard_stack>
## Standard Stack

### Core
| Tool / Artifact | Version | Purpose | Why Standard |
|-----------------|---------|---------|--------------|
| AW2 local client | 1.12.2 / 2.7.0 | primary runtime observation | runtime behavior outranks source assumptions |
| `run/logs/latest.log` | current session log | event and lifecycle evidence | low-friction, durable, timestamped evidence |
| `run/saves/` world data | current archaeology save | persistence and reload evidence | lets later phases inspect saved state directly |
| `golden_test_cases.md` | evolving artifact | canonical scenario capture doc | centralizes what was observed and expected |

### Supporting
| Tool / Artifact | Purpose | When to Use |
|-----------------|---------|-------------|
| screenshots folder | visual evidence | UI, selection, structure, command-tool flows |
| phase evidence index | quick evidence lookup | when one scenario references multiple logs/saves/screens |
| targeted source hotspots | implementation anchors | when a capture needs later Phase 3 mapping hints |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| one evolving golden doc | separate per-scenario markdown files | easier isolation, but harder to audit total coverage quickly |
| batching by subsystem | strict numerical order only | simpler ordering, but wastes setup reuse |

**Installation:** No new packages recommended for this phase. Use the rescued local runtime already established in Phase 1.
</standard_stack>

<architecture_patterns>
## Architecture Patterns

### Recommended Capture Structure
```
.planning/phases/02-golden-scenario-capture/
├── 02-CONTEXT.md
├── 02-RESEARCH.md
├── 02-EVIDENCE-INDEX.md
├── 02-BATCH-A-NOTES.md
├── 02-BATCH-B-NOTES.md
└── 02-BATCH-C-NOTES.md

golden_test_cases.md
```

### Pattern 1: Workbook First, Capture Second
Define scenario schema and evidence contract before recording any scenario.

### Pattern 2: Batch By Setup Reuse
Group scenarios that can be captured from the same world/session state.

### Pattern 3: Capture Now, Interpret Later
Phase 2 records what happened and what was expected; Phase 3 explains why and maps it to implementation.

### Anti-Patterns to Avoid
- source-first capture
- evidence drift
- mixed-scope notes
</architecture_patterns>

<dont_hand_roll>
## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Scenario spec format | ad-hoc notes per run | one canonical scenario schema in `golden_test_cases.md` | prevents inconsistent captures |
| Persistence evidence | memory-based descriptions | save file references and latest.log markers | avoids false recall |
| Multiplayer proof | “should sync” assumptions | explicit client/server observation and log-backed notes | sync bugs hide in assumptions |

**Key insight:** this phase is won by disciplined capture discipline, not by adding tooling complexity.
</dont_hand_roll>

<common_pitfalls>
## Common Pitfalls

### Pitfall 1: World State Drift
Document the world/save baseline for every scenario and fork saves when needed.

### Pitfall 2: Missing Evidence Links
Require an evidence field for every captured scenario before marking it complete.

### Pitfall 3: Mixing Capture With Explanation
Keep Phase 2 focused on setup, steps, observations, expected results, and blockers only.
</common_pitfalls>

<code_examples>
## Code Examples

### Standard launch command
```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk1.8.0_202'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat runClient
```

### Primary log evidence anchor
```text
AncientWarfare2-reference/run/logs/latest.log
```

### Persistence evidence anchor
```text
AncientWarfare2-reference/run/saves/New World/data/AWResearchData.dat
AncientWarfare2-reference/run/saves/New World/data/AWFactionData.dat
AncientWarfare2-reference/run/saves/New World/data/AWTeamData.dat
```
</code_examples>

<sota_updates>
## State of the Art (2024-2025)

| Older habit | Better current approach | Impact |
|-------------|-------------------------|--------|
| capture by memory after a play session | capture into the canonical doc immediately after each batch | fewer false assumptions |
| source-first reverse engineering | runtime-first archaeology with source hotspots as anchors | better parity fidelity |
| monolithic all-at-once recording | progressive batch capture with explicit blockers | better iteration and less context loss |
</sota_updates>

<open_questions>
## Open Questions

1. How much of the 12-scenario set can reuse a single save?
2. Do the missing sound assets or template warnings affect any target scenario materially?
</open_questions>

<sources>
## Sources

### Primary (HIGH confidence)
- `.planning/phases/01-legacy-reference-bring-up/01-LEGACY-REFERENCE-SETUP.md`
- `.planning/phases/01-legacy-reference-bring-up/01-01-SUMMARY.md`
- `AncientWarfare2-reference/run/logs/latest.log`
- `AncientWarfare2-reference/run/saves/New World/data/`
- targeted legacy source hotspots for research, warehouse, courier, builder, faction, and trade behavior

### Secondary (MEDIUM confidence)
- none; local project evidence was sufficient for planning this phase

### Tertiary (LOW confidence - needs validation)
- none
</sources>

## Validation Architecture

Phase 2 is primarily manual, but still benefits from explicit verification:

- every plan should verify that `golden_test_cases.md` was updated for the target scenarios
- every capture batch should prove evidence references exist and blockers are documented
- the final batch should verify all 12 scenarios exist in the canonical doc and none are silently omitted

<metadata>
## Metadata

**Research scope:** local runtime archaeology, evidence model, batching strategy
**Confidence breakdown:** capture strategy HIGH, environment assumptions HIGH, batching MEDIUM
**Research date:** 2026-04-11
**Valid until:** 2026-04-18
</metadata>

---

*Phase: 02-golden-scenario-capture*
*Research completed: 2026-04-11*
*Ready for planning: yes*
