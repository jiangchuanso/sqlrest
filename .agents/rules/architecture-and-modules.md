# 架构与模块约束

> 本文档描述项目模块边界、包结构与启动入口的约束。

## 模块职责

| 模块 | 职责 | 技术栈 |
|---|---|---|
| `sqlrest-manager` | 管理 API、**Eureka 服务端**、托管前端、启动时执行 DB 迁移 | WebMVC (Tomcat) + Liquibase |
| `sqlrest-gateway` | Spring Cloud Gateway，路由 API 流量 | WebFlux (响应式) |
| `sqlrest-executor` | 执行 SQL/DSL 查询，提供 Swagger/Knife4j 文档 | WebMVC (Tomcat) |
| `sqlrest-common` | 共享 DTO、工具类、配置 Bean、常量、枚举、异常 | lib |
| `sqlrest-template` | MyBatis 动态 SQL 模板引擎 | lib |
| `sqlrest-persistence` | MyBatis Mapper/DAO/Entity，系统库访问层 | lib |
| `sqlrest-core` | 核心 API/Service/Driver 逻辑，所有服务共享 | lib |
| `sqlrest-cache` | Hazelcast / Redis 缓存抽象 | lib |
| `sqlrest-mcp` | MCP 协议支持（子模块：`sqlrest-mcp-core`、`sqlrest-mcp-springmvc`） | lib |
| `sqlrest-dist` | 仅打包——通过 `maven-assembly-plugin` 组装发布 `tar.gz` | n/a |
| `sqlrest-manager-ui` | Vue 2 前端（独立 npm 项目，**不被 Maven 构建**） | Node |

## 包结构

根包为 `org.dromara.sqlrest`，各关注点的包位置：

| 关注点 | 包路径 | 模块 |
|---|---|---|
| 管理 Controller | `org.dromara.sqlrest.manager.controller` | sqlrest-manager |
| 执行 Controller | `org.dromara.sqlrest.executor.controller` | sqlrest-executor |
| Service | `org.dromara.sqlrest.core.service` | sqlrest-core |
| DAO | `org.dromara.sqlrest.persistence.dao` | sqlrest-persistence |
| Mapper | `org.dromara.sqlrest.persistence.mapper` | sqlrest-persistence |
| Entity | `org.dromara.sqlrest.persistence.entity` | sqlrest-persistence |
| 请求/响应 DTO | `org.dromara.sqlrest.core.dto` | sqlrest-core |
| 共享 DTO | `org.dromara.sqlrest.common.dto` | sqlrest-common |
| 配置 | `org.dromara.sqlrest.<module>.config` | 各服务 |
| 常量 | `org.dromara.sqlrest.common.consts` | sqlrest-common |
| 枚举 | `org.dromara.sqlrest.common.enums` | sqlrest-common |
| 异常 | `org.dromara.sqlrest.common.exception` | sqlrest-common |
| 工具类 | `org.dromara.sqlrest.common.util`、`core.util` | common/core |

## 启动入口与扫描包（关键约束）

三个可运行服务的入口类：

- `org.dromara.sqlrest.manager.ManagerApplication`
- `org.dromara.sqlrest.gateway.GatewayApplication`
- `org.dromara.sqlrest.executor.ExecutorApplication`

**每个 `@SpringBootApplication` 都显式声明 `scanBasePackages`**——新增包必须添加到对应应用的 `scanBasePackages` 中，否则不会被扫描。

示例（`ManagerApplication.java:24-35`）：
```java
@SpringBootApplication(scanBasePackages = {
    "org.dromara.sqlrest.persistence",
    "org.dromara.sqlrest.core.driver",
    "org.dromara.sqlrest.core.gateway",
    "org.dromara.sqlrest.core.executor",
    "org.dromara.sqlrest.core.service",
    "org.dromara.sqlrest.core.exec",
    "org.dromara.sqlrest.cache",
    "org.dromara.sqlrest.manager"
})
```

**所有三个服务都声明 `@MapperScan("org.dromara.sqlrest.persistence.mapper")`**——Mapper 接口必须放在 `sqlrest-persistence` 模块，不要放在服务模块。

## 启动顺序

Manager 是 Eureka 服务端。启动顺序必须为：**manager → executor → gateway**。

## WebMVC 与 WebFlux 类路径隔离

Manager 和 Executor 运行在 WebMVC (Tomcat)，Gateway 运行在 WebFlux (响应式)。
发布包 (`sqlrest-dist/src/main/assembly/package.xml`) 将 jar 分拆到 `lib/common`、`lib/webmvc`、`lib/webflux`、`lib/{manager,executor,gateway}`。
启动脚本 `sqlrestctl.sh` 按服务构建独立 classpath。

**禁止在同一 classpath 上混用 WebMVC 和 WebFlux 的 jar。**

## 配置流

运行时配置流：`conf/config.ini` (key=value) → `bin/sqlrestctl.sh` 读取 → 导出为环境变量 → Spring `${...}` 占位符消费。

- `DB_TYPE` 选择 Spring profile：`mysql` 或 `postgres`。
- `MANAGER_HOST` / `MANAGER_PORT` / `EXECUTOR_PORT` / `GATEWAY_PORT` 配置服务地址。
- Eureka `defaultZone` 指向 manager：`http://${MANAGER_HOST}:${MANAGER_PORT}/eureka/`。
- `SQLREST_DS_ENCRYPT` 控制数据源凭据静态加密。

## 版本管理

- 修改 `version.cmd` 中的 `APP_VERSION` 后执行：
  ```
  mvn versions:set -DnewVersion=X.Y.Z -DprocessAllModules=true -DgenerateBackupPoms=false
  mvn -N versions:update-child-modules
  mvn versions:commit
  ```

## JDBC 驱动

`drivers/` 目录包含各数据库的 JDBC jar，通过 `APP_DRIVERS_PATH` 环境变量在运行时加载。
**这些驱动不是 Maven 依赖**——不要在模块 `pom.xml` 中添加驱动 jar，应放入对应的 `drivers/<db>/` 目录。
