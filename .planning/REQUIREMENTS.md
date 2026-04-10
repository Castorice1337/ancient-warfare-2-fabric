# Requirements: Columbina

**Defined:** 2026-04-10
**Core Value:** 玩家在 Minecraft 1.21.11 中执行 AW2 核心玩法时，单机、联机和存档重进下的行为体验都应与 1.12.2 旧版保持可验证的一致性。

## v1 Requirements

### Reference Archaeology

- [ ] **ARCH-01**: 开发团队可以在本地启动固定版本的 AW2 `2.7.0` / Minecraft `1.12.2` 参考客户端与服务端环境
- [ ] **ARCH-02**: 开发团队可以按统一模板录制 12 个 Golden Reference 场景的前置条件、步骤、观察点与预期结果
- [ ] **ARCH-03**: 开发团队可以把每个目标系统拆解成 `input / state / output / boundary conditions` 四元行为描述
- [ ] **ARCH-04**: 开发团队可以把 Golden 场景映射到旧 AW2 的关键模块、类、存档点与同步点

### Research And Progression

- [ ] **RSCH-01**: 玩家在 1.21.11 版本中可以完成与旧版一致的研究树解锁流程，包括研究站、研究队列、研究资源消耗与结果解锁

### Logistics

- [ ] **LOGI-01**: 玩家可以创建仓库并通过扩容组件改变可用容量与库存可见性，行为与旧版一致
- [ ] **LOGI-02**: 玩家可以配置 courier 两点搬运路线，并看到其按库存能力、路径与搬运规则执行任务

### Labor

- [ ] **LABR-01**: 玩家可以使用 builder 按模板建房，并看到预览、建造进度、中断恢复与完成状态
- [ ] **LABR-02**: 玩家可以让至少两类工人（农民 / 伐木工 / 矿工）完成完整工作流，包含接单、取放物品、工作结果与异常恢复

### Combat And Command

- [ ] **COMB-01**: 玩家可以招募士兵并配置基础装备，使其进入可用战斗状态
- [ ] **COMB-02**: 玩家可以使用指挥工具完成选中、移动、巡逻、攻击四类基础命令
- [ ] **COMB-03**: 玩家可以建造并操作至少一种攻城器，并观察其攻击行为与伤害反馈

### World And Factions

- [ ] **WRLD-01**: 玩家可以在野外遭遇至少一种敌对结构，并完成生成、接敌、清剿闭环
- [ ] **WRLD-02**: 玩家可以与至少一个友方或中立阵营进行可观察交互，并使阵营关系、交易或站位反馈生效

### Multiplayer And Persistence

- [ ] **SYNC-01**: 多人联机时，NPC 状态、仓库库存和关键 UI 数据在客户端与服务端之间保持一致
- [ ] **SYNC-02**: 存档退出重进后，研究、仓库、订单、NPC、阵营与结构相关状态可以正确保留

### Regression Harness

- [ ] **QUAL-01**: 开发团队可以对目标实现重复执行 Golden 场景并输出与 1.12.2 参考行为的差异报告

## v2 Requirements

### Expanded Module Coverage

- **EXPD-01**: 玩家可以使用未纳入首版范围的 AW2 其他自动化与载具系统
- **EXPD-02**: 项目可以扩展到更多 NPC 子类型、结构模板和阵营内容
- **EXPD-03**: 项目可以支持更完整的内容兼容、迁移工具或跨加载器策略

## Out of Scope

| Feature | Reason |
|---------|--------|
| 一次性交付 AW2 全模块全内容 | 首版需要先拿下可验证的核心切片 |
| 逐文件 / 逐类直接翻译 Forge 实现 | 行为 parity 比源码形态更重要 |
| 在没有 Golden Reference 的情况下实现目标玩法 | 无法判断是否真的“像旧版” |
| 首版同时做多加载器支持 | 会显著增加同步、持久化与测试矩阵复杂度 |
| 先做新机制再补旧 AW2 parity | 会污染规格边界并拖慢核心复刻 |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| ARCH-01 | Phase 1 | Complete |
| ARCH-02 | Phase 2 | Pending |
| ARCH-03 | Phase 3 | Pending |
| ARCH-04 | Phase 3 | Pending |
| RSCH-01 | Phase 4 | Pending |
| LOGI-01 | Phase 5 | Pending |
| LOGI-02 | Phase 5 | Pending |
| LABR-01 | Phase 5 | Pending |
| LABR-02 | Phase 5 | Pending |
| COMB-01 | Phase 6 | Pending |
| COMB-02 | Phase 6 | Pending |
| COMB-03 | Phase 6 | Pending |
| WRLD-01 | Phase 6 | Pending |
| WRLD-02 | Phase 6 | Pending |
| SYNC-01 | Phase 7 | Pending |
| SYNC-02 | Phase 7 | Pending |
| QUAL-01 | Phase 7 | Pending |

**Coverage:**
- v1 requirements: 17 total
- Mapped to phases: 17
- Unmapped: 0

---
*Requirements defined: 2026-04-10*
*Last updated: 2026-04-10 after initial definition*
