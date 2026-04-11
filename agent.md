# agent.md

本文件给在仓库根目录工作的 AI/智能体阅读。

## 项目目标

Columbina-AW2 是 Ancient Warfare 2 的高版本直接移植，不是重制，也不是灵感重做。

默认原则：
- 能复用旧资源就复用
- 能复用旧模板就复用
- 能复用旧算法就复用
- 只重写 Fabric 1.21.11 必须重写的运行时兼容层

## 开始工作前的阅读顺序

1. `.planning/PROJECT.md`
2. `.planning/REQUIREMENTS.md`
3. `.planning/ROADMAP.md`
4. `.planning/STATE.md`
5. `.planning/MIGRATION-WORKFLOW.md`
6. `AncientWarfare2-reference` 中相关旧源码和旧资源路径

## 不可违背的规则

- 这是移植，不是重做
- 旧 AW2 的行为和内容是第一真相来源
- 不要擅自重设计仓库、courier、builder、research、command 这些系统
- 联机同步和存档持久化始终在主线范围内
- 缺失旧资源优先恢复，不要先重画或重写

## 当前实现约定

- 运行时 glue 使用 `columbina` 侧代码和入口
- 导入的旧内容命名空间保留为 `ancientwarfare`
- 资源/数据查找时要明确区分：
  - 运行时注册 id：`columbina`
  - 旧内容资源 id：`ancientwarfare`

## 当前阶段提醒

- Phase 3 已实现 runtime adapter skeleton 和 research station 的第一条垂直切片
- 当前仍有人工验证项待完成，见：
  - `.planning/phases/03-runtime-adapter-skeleton/03-VERIFICATION.md`
  - `.planning/phases/03-runtime-adapter-skeleton/03-HUMAN-UAT.md`

## 实现建议

- 规划时必须写出旧类名、旧资源路径、旧数据文件
- 把工作分成 `reuse directly`、`adapt`、`rewrite`
- 重写代码应该包裹/承载旧行为，不要静默替换成新设计
- 保留旧命名、旧图、旧节点关系、旧模板语义，除非现代运行时明确做不到

## 沟通语言

- 优先使用中文写说明、计划和执行记录
