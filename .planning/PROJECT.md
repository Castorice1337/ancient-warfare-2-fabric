# Columbina

## What This Is

Columbina 是一个面向 Minecraft 1.21.11 的 Ancient Warfare 2 高版本移植项目，目标不是照搬 1.12.2 代码，而是在现代 Fabric 环境中重建旧 AW2 的关键玩法闭环。项目把本地可运行的 AW2 1.12.2 参考环境和 `AncientWarfare2-reference` 源码仓当作“需求文档”，先考古行为，再进行高版本复刻。

## Core Value

玩家在 Minecraft 1.21.11 中执行 AW2 核心玩法时，单机、联机和存档重进下的行为体验都应与 1.12.2 旧版保持可验证的一致性。

## Requirements

### Validated

<!-- Shipped and confirmed valuable. -->

(None yet - ship to validate)

### Active

<!-- Current scope. Building toward these. -->

- [ ] 建立可重复启动的 AW2 1.12.2 本地参考环境
- [ ] 产出 `feature_inventory.md`、`behavior_spec.md`、`golden_test_cases.md`
- [ ] 在 Fabric 1.21.11 上复刻研究、仓库、NPC 劳作、战斗/指挥、结构/阵营、联机与持久化的目标切片

### Out of Scope

<!-- Explicit boundaries. Includes reasoning to prevent re-adding. -->

- 全量照抄 1.12.2 Forge 实现 - 会把旧技术债和 Forge 假设一起搬进新项目
- 第一个里程碑直接写高版本玩法代码 - 行为基线未固定前实现容易漂移
- 一次性覆盖 AW2 全部模块与全部内容 - 当前目标是先拿下定义好的核心垂直切片
- 同时支持 Fabric / Forge / NeoForge 多加载器 - 会显著放大架构和验证复杂度

## Context

- 当前工作区 `D:\1.21.11` 是一个几乎空白的 Fabric 1.21.11 模组骨架，项目名与模组 ID 仍是占位性质的 `columbina`
- 本地存在 `AncientWarfare2-reference` 参考仓，版本锚点为 Minecraft `1.12.2`、Forge `14.23.5.2816`、AW2 `2.7.0`
- 旧 AW2 参考仓的主要模块为 `core`、`automation`、`npc`、`structure`、`vehicle`
- 用户已明确两大里程碑：
  - 里程碑 1：旧版行为考古
  - 里程碑 2：高版本复刻
- 里程碑 1 的产物不是代码，而是三份文档：
  - `feature_inventory.md`
  - `behavior_spec.md`
  - `golden_test_cases.md`
- 里程碑 1 至少覆盖以下 12 个 Golden Reference 场景：
  - 研究树解锁流程
  - 仓库创建与扩容
  - courier 两点搬运
  - builder 按模板建房
  - 农民 / 伐木工 / 矿工中至少 2 类完整工作流
  - 士兵招募与装备
  - 指挥工具选中、移动、巡逻、攻击
  - 一个攻城器的建造与攻击
  - 一个野外敌对结构的生成与清剿
  - 一个友方 / 中立阵营交互
  - 多人联机下 NPC 和仓库是否同步
  - 存档退出重进后各种状态是否保留
- 从旧仓源码看，后续移植的高风险状态与同步热点集中在：
  - `ResearchTracker`
  - `TileResearchStation`
  - `TileWarehouse*`
  - `NpcAIPlayerOwnedCourier`
  - `NpcAIPlayerOwnedCommander`
  - `TradeOrder`
  - `TileStructureBuilder`
  - `FactionTracker`
  - `TerritoryData` / `StructureMap` / `TownMap`

## Constraints

- **Tech stack**: 目标仓固定在 Fabric `1.21.11`、Java `21`、Kotlin-enabled 项目骨架上 - 当前 repo 已按此栈初始化
- **Reference compatibility**: 行为参考必须锚定本地 AW2 `2.7.0` / Minecraft `1.12.2` / Forge `14.23.5.2816` - 避免“记忆版 AW2”偏差
- **Process**: 必须先完成行为考古文档再进行高版本复刻 - 这是里程碑边界，不是建议
- **Multiplayer**: 联机同步属于规格本身，不是最后补的优化项 - 用户已把联机同步列进 Golden 场景
- **Persistence**: 存档退出重进后的状态保留属于规格本身 - 用户已把存档恢复列进 Golden 场景
- **Scope control**: 首版目标是拿下指定垂直切片而非 AW2 全内容 - 防止移植范围失控

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| 把旧 AW2 当需求文档而不是实现模板 | 目标是行为复刻，不是源码搬运 | Pending |
| 用本地 1.12.2 运行结果建立 Golden Reference | README 和记忆都不足以定义边界行为 | Pending |
| 先做考古里程碑再写移植代码 | 没有规格就无法验证高版本行为是否正确 | Pending |
| 以 Fabric 1.21.11 为唯一首要目标 | 当前仓已固定该方向，避免多加载器分心 | Pending |
| 把联机同步和存档持久化纳入主规格 | 这两项在旧 AW2 中跨系统耦合最强，晚做会返工 | Pending |
| 先复刻用户点名的 12 个场景所覆盖的系统 | 这是最小可信 parity 面，而不是“全功能”口号 | Pending |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `$gsd-transition`):
1. Requirements invalidated? -> Move to Out of Scope with reason
2. Requirements validated? -> Move to Validated with phase reference
3. New requirements emerged? -> Add to Active
4. Decisions to log? -> Add to Key Decisions
5. "What This Is" still accurate? -> Update if drifted

**After each milestone** (via `$gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check -> still the right priority?
3. Audit Out of Scope -> reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-04-10 after initialization*
