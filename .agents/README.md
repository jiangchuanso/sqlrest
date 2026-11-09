# .agents

本目录为 AI 编程工具提供项目上下文，包含三类资源：

| 目录 | 用途 |
|---|---|
| `commands/` | 可调用的命令（`opsx-*` OpenSpec 工作流命令） |
| `skills/` | 专用技能说明（OpenSpec 提案、实现、归档、探索） |
| `rules/` | 项目代码规范与约束说明文档 |

## 命令 (commands)

| 命令 | 说明 |
|---|---|
| `opsx-propose` | 一步提案新变更并生成所有产出物（proposal/design/tasks） |
| `opsx-apply` | 实现 OpenSpec 变更中的任务 |
| `opsx-archive` | 归档已完成的变更 |
| `opsx-explore` | 进入探索模式，构思想法、调查问题、澄清需求 |

## 技能 (skills)

| 技能 | 说明 |
|---|---|
| `openspec-propose` | 提案新变更的详细工作流 |
| `openspec-apply-change` | 实现变更任务的详细工作流 |
| `openspec-archive-change` | 归档变更的详细工作流 |
| `openspec-explore` | 探索模式的姿态与护栏 |
| `add-relational-datasource` | 扩展关系型数据库数据源的详细工作流 |

> OpenSpec 技能和命令需要 `openspec-cn` CLI。`add-relational-datasource` 技能不需要额外 CLI。

## 规则 (rules)

| 文档 | 覆盖范围 |
|---|---|
| `java-code-style.md` | 缩进、版权头、Lombok、注释语言、导入 |
| `architecture-and-modules.md` | 模块边界、包结构、入口类、扫描包约束 |
| `api-and-controller.md` | Controller 约定、统一返回、URL 规范、HTTP 方法 |
| `persistence-and-mybatis.md` | 双层 DAO/Mapper、无 XML、多数据库 SQL 分支、实体约定 |
| `service-layer.md` | Service 约定、依赖注入、事务、DTO 转换 |
| `exception-and-i18n.md` | 全局异常处理、CommonException、错误码、i18n |
| `frontend-vue.md` | Vue 2 前端约定 |

编写代码前请先阅读对应的规则文档，避免违反既定约定。
