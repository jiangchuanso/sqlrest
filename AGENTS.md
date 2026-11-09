# AGENTS.md

Guidance for AI agents working in the sqlrest repository.

## Build & Test

- **Java 8 required** (source/target 1.8). Spring Boot 2.5.6, Spring Cloud 2020.0.4.
- Build (skips tests by default — `maven.test.skip=true` is set in the parent `pom.xml`):
  ```
  mvn clean package -Dmaven.test.skip=true
  ```
  `build.cmd` / `build.sh` wrap this. Docker build: `sh docker-maven-build.sh`.
- Tests are JUnit 4 and are **skipped by default**. Only two test files exist
  (`sqlrest-core` JacksonUtilsTest, `sqlrest-template` TemplateTest). To run them:
  ```
  mvn test -Dmaven.test.skip=false
  ```
- Bump the version across all modules (edit `APP_VERSION` in `version.cmd` first):
  ```
  mvn versions:set -DnewVersion=X.Y.Z -DprocessAllModules=true -DgenerateBackupPoms=false
  mvn -N versions:update-child-modules
  mvn versions:commit
  ```

## Architecture

Multi-module Maven project with three runnable Spring Boot services and shared libraries.

| Module | Role | Stack |
|---|---|---|
| `sqlrest-manager` | Admin API, **Eureka server**, serves the built frontend, runs DB migrations on startup | WebMVC (Tomcat) + Liquibase |
| `sqlrest-gateway` | Spring Cloud Gateway, routes API traffic | WebFlux (reactive) |
| `sqlrest-executor` | Executes SQL/DSL queries against user data sources; serves Swagger/Knife4j docs | WebMVC (Tomcat) |
| `sqlrest-common` | Shared DTOs, utils, config beans | lib |
| `sqlrest-template` | MyBatis dynamic SQL template engine | lib |
| `sqlrest-persistence` | MyBatis mappers for the sqlrest system schema | lib |
| `sqlrest-core` | Core API/service/driver logic shared by all services | lib |
| `sqlrest-cache` | Hazelcast / Redis cache abstraction | lib |
| `sqlrest-mcp` | MCP protocol support (submodules: `sqlrest-mcp-core`, `sqlrest-mcp-springmvc`) | lib |
| `sqlrest-dist` | Packaging only — assembles the release `tar.gz` via `maven-assembly-plugin` | n/a |
| `sqlrest-manager-ui` | Vue 2 frontend (separate npm project, **not** built by Maven) | Node |

- **Manager is the Eureka server.** Startup order matters: `manager -> executor -> gateway`.
- **Entrypoints**: `ManagerApplication`, `GatewayApplication`, `ExecutorApplication` (each in
  `org.dromara.sqlrest.<module>`). Each declares explicit `scanBasePackages` — new packages
  must be added to the relevant application's `@SpringBootApplication` or they won't be scanned.
- **All three services use `@MapperScan("org.dromara.sqlrest.persistence.mapper")`** — mappers
  live in `sqlrest-persistence`, not in the service modules.

## Classpath separation (gotcha)

The release assembly (`sqlrest-dist/src/main/assembly/package.xml`) splits jars into
`lib/common`, `lib/webmvc`, `lib/webflux`, and `lib/{manager,executor,gateway}` because
manager+executor run on WebMVC (Tomcat) while gateway runs on WebFlux. The startup script
`sqlrestctl.sh` builds per-service classpaths accordingly. **Do not mix WebMVC and WebFlux
jars on the same classpath** — this is why the assembly has so many explicit include/exclude rules.

## Configuration flow

Runtime config flows: `conf/config.ini` (key=value) → read by `bin/sqlrestctl.sh` →
exported as env vars → consumed by Spring `${...}` placeholders in `application.yaml`.

- `DB_TYPE` selects the Spring profile: `mysql` or `postgres` (loads
  `application-mysql.yaml` / `application-postgres.yaml`).
- `MANAGER_HOST` / `MANAGER_PORT` / `EXECUTOR_PORT` / `GATEWAY_PORT` configure service addresses.
- Eureka `defaultZone` points at the manager: `http://${MANAGER_HOST}:${MANAGER_PORT}/eureka/`.
- `SQLREST_DS_ENCRYPT` toggles at-rest encryption of datasource credentials.

## Database migrations

Manager runs **Liquibase** on startup. Changelogs and SQL live in
`sqlrest-manager/src/main/resources/`:
- MySQL: `db/changelog/` + `db/migration/V*__*.sql`
- PostgreSQL: `pg/changelog/` + `pg/migration/V*__*.sql`

Custom changelog table names: `SQLREST_DB_CHANGE_LOG_RECORD` /
`SQLREST_DB_CHANGE_LOG_LOCK`. When adding schema changes, append a new `log-vX.Y.Z.yaml`
changelog file and register it in `db.changelog-master.yaml`.

## Frontend (sqlrest-manager-ui)

The UI is a **separate Vue 2 + webpack** project and is **not** built by Maven.

1. `cd sqlrest-manager-ui && npm install` (Node >= v14.15.4)
2. `npm run build`
3. **Manually copy** `sqlrest-manager-ui/dist/*` into
   `sqlrest-manager/src/main/resources/` (overwriting `static/`, `index.html`).

The pre-built artifacts are committed under `sqlrest-manager/src/main/resources/static/`, so
a normal Maven build picks them up without rebuilding the frontend.

## JDBC drivers

`drivers/` contains per-database JDBC jars (oracle, mysql, postgresql, clickhouse, hive,
mongodb, elasticsearch, http, etc.). These ship in the release tarball and are loaded at
runtime via the `APP_DRIVERS_PATH` env var. They are **not** Maven dependencies — do not add
driver jars to module `pom.xml` files; drop them in the appropriate `drivers/<db>/` folder.

## .agents (必读)

**开始任何任务前，必须先读取 `.agents/` 目录下的相关内容**，它为 AI 编程工具提供项目上下文：

- **`.agents/README.md`** — 先读此文件，了解 `.agents/` 的整体结构与资源索引。
- **`.agents/rules/`** — 项目代码规范与约束。编写代码前阅读对应规则文档，避免违反既定约定：
  - `java-code-style.md`（缩进、版权头、Lombok、注释语言、导入）
  - `architecture-and-modules.md`（模块边界、包结构、入口类、扫描包约束）
  - `api-and-controller.md`（Controller 约定、统一返回、URL 规范）
  - `persistence-and-mybatis.md`（DAO/Mapper、多数据库 SQL 分支、实体约定）
  - `service-layer.md`（Service 约定、依赖注入、事务、DTO 转换）
  - `exception-and-i18n.md`（全局异常、错误码、i18n）
  - `frontend-vue.md`（Vue 2 前端约定）
- **`.agents/skills/`** — 专用技能说明，描述特定任务的工作流与护栏。当任务匹配某技能时，按其步骤执行：
  - `openspec-propose` / `openspec-apply-change` / `openspec-archive-change` / `openspec-explore`（OpenSpec 工作流）
  - `add-relational-datasource`（扩展关系型数据库数据源）
- **`.agents/commands/`** — 可调用的命令（`opsx-*` OpenSpec 工作流命令）。

## OpenSpec

- OpenSpec skills (`openspec-propose`, `openspec-apply-change`, `openspec-archive-change`,
  `openspec-explore`) are available via the `opsx-*` commands.
