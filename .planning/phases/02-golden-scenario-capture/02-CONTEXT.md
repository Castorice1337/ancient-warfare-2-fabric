# Phase 2: Golden Scenario Capture - Context

**Gathered:** 2026-04-11
**Status:** Ready for planning

<domain>
## Phase Boundary

Capture the 12 required AW2 Golden Reference scenarios in a standardized way using the now-runnable 1.12.2 reference environment. This phase is about reproducible scenario execution, evidence collection, and scenario-level expected-result documentation. It is not yet the phase for full behavior decomposition into `input / state / output / boundary conditions`.

</domain>

<decisions>
## Implementation Decisions

### Reference environment
- Use `AncientWarfare2-reference` as the only execution environment for Golden capture.
- Use the rescued Java 8 + Gradle 4.8 boot path from Phase 1 as the standard launch path.
- Treat the current integrated singleplayer world path as the default starting point unless a scenario explicitly requires a fresh save or multiplayer.

### Primary deliverable
- Use `golden_test_cases.md` as the evolving canonical capture document for this phase.
- Record each scenario with `setup / steps / observations / expected results / evidence / blockers`.

### Evidence policy
- Every scenario must point to at least one durable evidence source: text notes, screenshots, or log references.
- `AncientWarfare2-reference/run/logs/latest.log` is the default log anchor.
- Save-specific state evidence may also reference files under `AncientWarfare2-reference/run/saves/`.

### Capture strategy
- Batch scenarios by setup reuse so the same world/session can serve multiple captures where practical.
- Separate singleplayer core loops from multiplayer/persistence checks.
- Explicitly note every blocker instead of silently omitting unstable scenarios.

### Scope boundaries
- Phase 2 stops at scenario capture and scenario-level expected behavior.
- `feature_inventory.md` and `behavior_spec.md` belong to Phase 3, though Phase 2 notes may seed them.

### the agent's Discretion
- Exact scenario ordering inside each capture batch
- Whether to use one reusable world or multiple saves for different batches
- How to structure evidence index files under the phase directory

</decisions>

<specifics>
## Specific Ideas

- The 12 required scenarios are already fixed by the project brief and should be copied into the main test document, not rediscovered.
- The current successful `New World` save and its persisted files (`AWResearchData.dat`, `AWFactionData.dat`, `AWTeamData.dat`) are valuable anchors for persistence-related captures.
- Missing sound assets and non-fatal structure warnings should be treated as known caveats, not automatic blockers, unless they directly interfere with a target scenario.

</specifics>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project scope
- `.planning/PROJECT.md` - overall project framing and milestone boundaries
- `.planning/ROADMAP.md` - Phase 2 goal and success criteria
- `.planning/REQUIREMENTS.md` - `ARCH-02` requirement this phase addresses
- `.planning/STATE.md` - current focus and open risks

### Phase 1 runtime baseline
- `.planning/phases/01-legacy-reference-bring-up/01-LEGACY-REFERENCE-SETUP.md` - rescued launch path and known caveats
- `.planning/phases/01-legacy-reference-bring-up/01-01-SUMMARY.md` - execution outcomes and caveats
- `AncientWarfare2-reference/run/logs/latest.log` - successful local runtime evidence

### Legacy hotspots for capture targeting
- `AncientWarfare2-reference/src/main/java/net/shadowmage/ancientwarfare/core/research/ResearchTracker.java` - research progress state
- `AncientWarfare2-reference/src/main/java/net/shadowmage/ancientwarfare/core/tile/TileResearchStation.java` - research station runtime behavior
- `AncientWarfare2-reference/src/main/java/net/shadowmage/ancientwarfare/automation/tile/warehouse2/TileWarehouse.java` - warehouse authority and inventory state
- `AncientWarfare2-reference/src/main/java/net/shadowmage/ancientwarfare/npc/ai/owned/NpcAIPlayerOwnedCourier.java` - courier task execution
- `AncientWarfare2-reference/src/main/java/net/shadowmage/ancientwarfare/structure/tile/TileStructureBuilder.java` - builder/template construction
- `AncientWarfare2-reference/src/main/java/net/shadowmage/ancientwarfare/npc/faction/FactionTracker.java` - faction standing state
- `AncientWarfare2-reference/src/main/java/net/shadowmage/ancientwarfare/npc/orders/TradeOrder.java` - route/order persistence and logistics behavior

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- The successful `run/saves/New World/` world can act as the first working archaeology save.
- Existing log output already confirms mod load, world join, and save/quit behavior.

### Established Patterns
- Cross-system state is distributed across world data files, tile/entity NBT, UI containers, and explicit packets.
- Scenario capture should therefore combine runtime observation with save/log references where needed.

### Integration Points
- The main evolving artifact for this phase should be `golden_test_cases.md`.
- Phase notes under `.planning/phases/02-golden-scenario-capture/` can hold evidence index and batch-specific working notes.

</code_context>

<deferred>
## Deferred Ideas

- Full system inventory and taxonomy output
- Input/state/output/boundary decomposition
- Mapping every captured behavior back to all source files and persistence points

</deferred>

---

*Phase: 02-golden-scenario-capture*
*Context gathered: 2026-04-11*
