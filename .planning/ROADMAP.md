# Roadmap: Columbina

**Created:** 2026-04-10
**Mode:** Interactive
**Granularity:** Standard
**Coverage:** 17 / 17 v1 requirements mapped

**Execution status (2026-04-10):** Phase 1 completed. The local AW2 1.12.2 reference environment now boots and reaches an in-world playable state.

## Overview

| Phase | Milestone | Name | Goal | Requirements |
|-------|-----------|------|------|--------------|
| 1 | Milestone 1 | Legacy Reference Bring-Up | 跑通本地 1.12.2 AW2 参考环境并固定复现方式 | ARCH-01 |
| 2 | Milestone 1 | Golden Scenario Capture | 录制 12 个 Golden Reference 场景并稳定复现 | ARCH-02 |
| 3 | Milestone 1 | Behavior Spec And Source Mapping | 把行为拆成规格并映射旧仓关键实现点 | ARCH-03, ARCH-04 |
| 4 | Milestone 2 | Modern Port Foundations | 在 1.21.11 上建立可承载 parity 的基础运行时与研究进度闭环 | RSCH-01 |
| 5 | Milestone 2 | Logistics And Labor Vertical Slice | 复刻仓库、搬运、建造与两类工人闭环 | LOGI-01, LOGI-02, LABR-01, LABR-02 |
| 6 | Milestone 2 | Combat, Structures, And Factions Slice | 复刻士兵、指挥、攻城器、敌对结构与阵营交互 | COMB-01, COMB-02, COMB-03, WRLD-01, WRLD-02 |
| 7 | Milestone 2 | Multiplayer, Persistence, And Regression | 把联机同步、存档保留与 Golden 回归闭环补齐 | SYNC-01, SYNC-02, QUAL-01 |

## Milestone 1: Legacy Behavior Archaeology

### Phase 1: Legacy Reference Bring-Up

Goal: 在本地跑起 AW2 1.12.2 参考环境，并把启动、依赖、日志、存档位置和复现方式固定下来。

Requirements: ARCH-01

Success criteria:
1. 本地可以稳定启动参考客户端与至少一种联机验证路径（局域网或独立服务端）
2. 运行所需的 Java、Forge、模组、配置和启动命令被完整记录
3. 参考世界、日志目录与截图 / 录像落点被固定，便于后续复测
4. 任何未解决的环境阻塞都被显式登记并有下一步方案

### Phase 2: Golden Scenario Capture

Goal: 录出 12 个 Golden Reference 场景，把“怎么做、看什么、算成功还是失败”标准化。

Requirements: ARCH-02

Success criteria:
1. 12 个场景全部有统一格式的 setup / steps / observations / expected results
2. 每个场景都标明是否需要作弊指令、世界 seed、预置存档或联机环境
3. 每个场景都至少有一种证据形式可回看（文本记录、截图、录像、日志）
4. 无法稳定复现的场景被单独列为 blocker，而不是混入“已完成”

### Phase 3: Behavior Spec And Source Mapping

Goal: 把 Golden 场景沉淀成真正能指导移植的规格文档，而不是零散记录。

Requirements: ARCH-03, ARCH-04

Success criteria:
1. `feature_inventory.md` 完成系统级功能盘点
2. `behavior_spec.md` 完成 input / state / output / boundary conditions 拆解
3. `golden_test_cases.md` 完成可重复执行的回归用例定义
4. 每个目标系统都能追溯到旧仓中的关键类、关键状态与关键同步 / 持久化点

## Milestone 2: High-Version Port

### Phase 4: Modern Port Foundations

Goal: 在 Fabric 1.21.11 上建立最小可运行的基础设施，并先拿下研究系统这个跨 UI、持久化、同步的基础闭环。

Requirements: RSCH-01

Success criteria:
1. 目标模组在客户端与服务端都能稳定启动，并具备基础网络与存档骨架
2. 研究相关的数据模型、持久化入口与同步边界被明确实现
3. 研究站 / 研究队列 / 解锁判定形成第一个端到端垂直切片
4. 研究闭环可以和里程碑 1 的 Golden Reference 做逐项对比

### Phase 5: Logistics And Labor Vertical Slice

Goal: 先拿下 AW2 最核心的后勤与劳作系统，让“仓库 -> 搬运 -> 建造 / 工作”形成现代版本中的可玩闭环。

Requirements: LOGI-01, LOGI-02, LABR-01, LABR-02

Success criteria:
1. 仓库创建、扩容和库存可见性能以权威服务端状态运作
2. courier 可以完成两点搬运并正确响应库存 / 路径 / 容器能力边界
3. builder 可以按模板执行建造、显示进度并处理中断恢复
4. 至少两类工人可以完成完整工作流并在缺料、阻塞、背包满等场景下恢复

### Phase 6: Combat, Structures, And Factions Slice

Goal: 把战斗、指挥、攻城、野外结构和阵营交互做成第二个可玩垂直切片。

Requirements: COMB-01, COMB-02, COMB-03, WRLD-01, WRLD-02

Success criteria:
1. 士兵招募与装备流程可用，且状态切换清晰可观察
2. 指挥工具支持选中、移动、巡逻、攻击四个基础命令
3. 至少一种攻城器具备建造与攻击闭环
4. 至少一种敌对结构具备生成、接敌、清剿与后状态反馈
5. 至少一个友方 / 中立阵营具备可观察交互与关系反馈

### Phase 7: Multiplayer, Persistence, And Regression

Goal: 把“能玩”收束成“可信 parity”，即联机一致、存档可靠、回归可复跑。

Requirements: SYNC-01, SYNC-02, QUAL-01

Success criteria:
1. 联机环境下 NPC、仓库和关键 UI 状态能在客户端和服务端保持一致
2. 存档退出重进后，研究、仓库、订单、NPC、阵营与结构状态全部保留
3. Golden 场景可以在目标版本重复执行并输出差异报告
4. 所有剩余 parity 缺口都有明确归档：bug、缺失功能或设计性偏离
5. 形成继续扩展更多 AW2 子系统的可持续回归基线

## Coverage Check

- Total v1 requirements: 17
- Covered by roadmap: 17
- Uncovered: 0
- Current next phase: Phase 2 - Golden Scenario Capture

---
*Roadmap created: 2026-04-10*
