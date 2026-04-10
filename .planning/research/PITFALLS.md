# Pitfalls Research: Columbina

| Pitfall | Warning Signs | Prevention Strategy | Phase |
|---------|---------------|---------------------|-------|
| Treating legacy source as implementation instead of specification | Work starts with class-by-class port plans instead of scenario/spec docs | Force every target feature to cite a Golden scenario and behavior spec section first | Phases 1-7 |
| Ignoring multiplayer until late | Singleplayer demos work, but there is no clear sync owner for inventories, orders, or NPC state | Define server authority and sync surfaces while designing each subsystem | Phases 4-7 |
| Under-specifying persistence | Features look fine in-session but break after save/reload | Add save/reload checks to each vertical slice before calling it complete | Phases 4-7 |
| Only porting happy paths | Demo cases work, but missing inputs, blocked routes, or partial progress cause undefined behavior | Capture boundary conditions explicitly in `behavior_spec.md` and regression cases | Phases 2-7 |
| Rebuilding too many systems before one full slice closes | Many partial systems exist, but nothing is fully comparable to legacy AW2 | Keep the roadmap vertical-slice oriented and enforce requirement-to-phase mapping | Phases 4-7 |
| Letting template/build semantics drift | Builder visuals exist, but progress, recovery, or inventory sourcing no longer match old AW2 | Anchor builder work to template, progress, and interruption behaviors documented in archaeology | Phase 5 |
| Losing traceability between docs and code | Team can no longer explain which old behavior a new implementation is supposed to match | Require legacy hotspot references in phase plans and implementation notes | Phases 3-7 |

## Top Three Risks

1. State ownership drift between legacy behavior and modern Fabric implementation
2. UI-visible systems appearing correct while server state or persistence is wrong
3. Scope creep into “all of AW2” before the required 12 scenarios are truly locked down
