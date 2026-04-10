# Phase 1: Legacy Reference Bring-Up - Context

**Gathered:** 2026-04-10
**Status:** Ready for planning

<domain>
## Phase Boundary

Bring the local AW2 1.12.2 reference environment into a repeatable working state. This phase is only about bootstrapping the legacy reference client/toolchain and documenting how to reproduce it. Golden scenario capture and behavior decomposition are separate phases.

</domain>

<decisions>
## Implementation Decisions

### Reference anchor
- Use `AncientWarfare2-reference` as the local source-of-truth workspace for legacy behavior.
- Treat the runnable 1.12.2 environment as the authoritative reference, not the old README alone.

### Runtime choices
- Use Java 8 (`C:\Program Files\Java\jdk1.8.0_202`) for the legacy Forge 1.12.2 toolchain.
- Keep the legacy Gradle wrapper (`gradle-4.8`) intact and rescue compatibility around it instead of modernizing the whole build.

### Compatibility rescue scope
- Apply only minimal patches required to make the reference environment boot locally.
- Move optional compat mods (`Electroblob's Wizardry`, `InfinityLib`, `AgriCraft`) out of the default local runtime path if they block startup.
- Accept missing sound assets and template warning noise for Phase 1 as long as the client reaches an in-world playable state.

### the agent's Discretion
- Exact wording and structure of the setup notes
- How much low-level build rescue detail to include in the phase summary

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project scope
- `.planning/PROJECT.md` - overall project intent, constraints, and migration framing
- `.planning/ROADMAP.md` - phase boundary and success criteria for Phase 1
- `.planning/REQUIREMENTS.md` - `ARCH-01` requirement this phase must satisfy
- `.planning/STATE.md` - current project position and phase status

### Legacy reference workspace
- `AncientWarfare2-reference/build.gradle` - rescued dependency and repository configuration
- `AncientWarfare2-reference/gradle.properties` - pinned legacy version properties and Cursemaven file IDs
- `AncientWarfare2-reference/src/main/java/net/shadowmage/ancientwarfare/core/manual/ManualContentRegistry.java` - local null-guard compatibility patch required for boot

### Runtime evidence
- `AncientWarfare2-reference/run/logs/latest.log` - boot, world join, integrated server, and shutdown evidence

</canonical_refs>

<specifics>
## Specific Ideas

- Phase 1 only needs to prove that the local legacy environment is reproducibly runnable.
- A successful integrated singleplayer world join is enough to call the bring-up successful.
- Dedicated server and multiplayer-specific reference checks can begin in Phase 2.

</specifics>

<code_context>
## Existing Code Insights

### Reusable Assets
- `AncientWarfare2-reference` already contains the full old AW2 modules and build scripts.

### Established Patterns
- Optional compat integrations are already structured as gated compat code in AW2; the local rescue should preserve that spirit.

### Integration Points
- Build rescue happens primarily in Gradle dependency resolution and one manual-content startup path.

</code_context>

<deferred>
## Deferred Ideas

- Mirroring or repairing all missing Mojang sound asset downloads
- Cleaning or validating every failing structure template load
- Dedicated server bring-up and multiplayer synchronization checks

</deferred>

---

*Phase: 01-legacy-reference-bring-up*
*Context gathered: 2026-04-10*
