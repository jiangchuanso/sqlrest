---
name: add-relational-datasource
description: 扩展关系型数据库数据源。当用户想要在 sqlrest 中新增一种关系型数据库的数据源支持（如 StarRocks、Doris、Greenplum 等）时使用。
license: MIT
compatibility: 需要 JDK 8、Maven 构建环境。
metadata:
  author: sqlrest
  version: "1.0"
---

扩展关系型数据库数据源 - 在 sqlrest 中新增一种关系型数据库的数据源类型。

我将完成以下两个核心步骤：
- 在 `ProductTypeEnum` 中新增枚举值（配置数据库元数据）
- 在 `drivers/` 目录下放置驱动 jar 文件（提供 JDBC 驱动）

详细原理与属性说明请参考开发文档：`docs/develop/add-relational-datasource.md`

---

## 触发方式

本技能文件存在于位置：

| 位置 | 触发方式 |
|------|----------|
| `.agents/skills/add-relational-datasource/SKILL.md` | **手动触发** — Agent 依据 `AGENTS.md` 中 `.agents (必读)` 的指引，主动读取此文件并按步骤执行。 |

### 触发提示词

当用户输入包含 **"增加/新增/扩展/添加" + "数据源/数据库"** 语义的请求时，应触发本技能。典型提示词示例：

| 提示词 | 说明 |
|--------|------|
| `增加一个 StarRocks 数据源` | 指定具体数据库名称 |
| `我想新增支持 Greenplum 数据库` | 明确"新增支持"意图 |
| `帮我扩展一个 Doris 数据源类型` | 明确"扩展"意图 |
| `sqlrest 添加对 XX 数据库的支持` | 带项目名 + "添加支持" |
| `新增一个关系型数据库数据源` | 泛化请求，技能会询问具体数据库 |
| `支持 XX 数据库` | 简短表述，语义匹配"新增支持" |

### 触发判断规则

1. **匹配关键词**：请求中同时出现"动作词"（增加/新增/扩展/添加/支持）和"目标词"（数据源/数据库）。
2. **排除非触发场景**：
   - 修改**已有**数据源的配置（如"修改 MySQL 数据源的端口"）— 不触发
   - 查询/排查**已有**数据源的问题（如"StarRocks 连接失败怎么排查"）— 不触发
   - 仅询问概念性问题（如"怎么扩展数据源"）— 不触发，直接回答
3. **模糊处理**：如果请求模糊（如只说"加个数据库"），仍触发技能，在步骤 1 中通过提问明确具体数据库。

---

**输入**：用户的请求应包含目标数据库的名称，以及（可选）以下信息：
- 数据库的 JDBC 驱动类名
- JDBC URL 前缀与格式
- 默认端口号
- 是否兼容 MySQL/PostgreSQL 等已有协议
- 分页 SQL 语法
- 标识符引用符号

**步骤**

1. **如果没有提供明确的数据库信息，询问用户**

   使用 **AskUserQuestion tool** 询问：
   > "您想要新增哪种关系型数据库的数据源支持？请提供数据库名称，以及（如已知）JDBC 驱动类名、URL 前缀、默认端口等信息。"

   **重要提示**：在不了解目标数据库的情况下，请勿继续。

2. **阅读参考文档与规则**

   开始编码前，必须先阅读以下文件：
   - `docs/develop/add-relational-datasource.md` — 完整的开发指南与属性说明
   - `.agents/rules/java-code-style.md` — Java 代码规范（版权头、缩进、Lombok 等）

   使用 **Read tool** 读取这两个文件。

3. **阅读现有代码以确定参数**

   使用 **Read tool** 读取以下文件：
   - `sqlrest-common/src/main/java/org/dromara/sqlrest/common/enums/ProductTypeEnum.java` — 查看已有枚举值，确定下一个可用的 `id`，并参考同协议数据库（如 MySQL 系）的配置。
   - `sqlrest-common/src/main/java/org/dromara/sqlrest/common/dto/ProductContext.java` — 确认所有可用属性。

   关键确认事项：
   - **id**：取未被使用的最小正整数（查看所有已有枚举的 `.id(...)` 值）
   - **name**：全小写，与 `drivers/` 目录名匹配
   - **driver**：JDBC 驱动类全限定名，必须与 jar 包中的实际类名一致
   - **quote**：标识符引用符号（MySQL 系用反引号 `` ` ``，PG 系用双引号 `"`）
   - **urlPrefix**：JDBC URL 前缀
   - **pageSql** / **pageConsumer**：分页 SQL 模板与参数填充函数，二者必须配合

4. **判断是否为协议兼容型数据库**

   如果目标数据库兼容 MySQL 或 PostgreSQL 协议（如 StarRocks、Doris 兼容 MySQL；Greenplum 兼容 PostgreSQL），直接复用对应协议的全部配置：
   - MySQL 兼容：driver=`com.mysql.jdbc.Driver`，urlPrefix=`jdbc:mysql://`，quote=`` ` ``，testSql=`/* ping */ SELECT 1`，pageSql=`%s LIMIT ? OFFSET ? `
   - PostgreSQL 兼容：driver=`org.postgresql.Driver`，urlPrefix=`jdbc:postgresql://`，quote=`"`，testSql=`SELECT 1`，pageSql=`%s LIMIT ? OFFSET ? `

   如果是独立协议的数据库，需查阅其 JDBC 驱动文档确定上述参数。

5. **在 ProductTypeEnum 中新增枚举值**

   使用 **Edit tool** 在 `ProductTypeEnum.java` 的枚举值列表中（分号 `;` 之前）新增枚举值。

   枚举值必须遵循以下格式（以 STARROCKS 为例）：

   ```java
   /**
    * StarRocks数据库类型
    */
   STARROCKS(
       ProductContext.builder()
           .id(19)
           .quote("`")
           .name("starrocks")
           .driver("com.mysql.jdbc.Driver")
           .defaultPort(3306)
           .testSql("/* ping */ SELECT 1")
           .urlPrefix("jdbc:mysql://")
           .tplUrls(new String[]{"jdbc:mysql://{host}[:{port}]/[{database}][\\?{params}]"})
           .urlSample(
               "jdbc:mysql://172.17.2.10:3306/test?useUnicode=true&characterEncoding=utf-8&useSSL=false&zeroDateTimeBehavior=convertToNull&serverTimezone=Asia/Shanghai&tinyInt1isBit=false&rewriteBatchedStatements=true&useCompression=true")
           .sqlSchemaList("SELECT `SCHEMA_NAME` FROM `information_schema`.`SCHEMATA`")
           .adapter(database -> Pair.of(database, null))
           .pageSql("%s LIMIT ? OFFSET ? ")
           .pageConsumer(
               (page, size, parameters) -> {
                 parameters.add(size);
                 parameters.add((page - 1) * size);
               }
           ).build()),
   ```

   **编写注意事项**：
   - 版权头已存在于文件顶部，不要修改
   - 保持 2 空格缩进，与已有枚举值风格一致
   - `name` 必须全小写
   - `id` 不可与已有枚举值重复
   - `adapter`：MySQL 系用 `database -> Pair.of(database, null)`，PG 系用 `database -> Pair.of(null, database)`
   - `pageConsumer` 的参数添加顺序必须与 `pageSql` 中 `?` 的出现顺序一致
   - 普通关系型数据库不设置 `multiDialect`、`retSchemaList`、`noViewTables`、`hasCatalogAndSchema`、`executeBeforeQuery`、`resultSetFunc`

6. **在 drivers 目录下放置驱动 jar 文件**

   在 `drivers/` 下创建目录结构：

   ```
   drivers/<枚举name小写>/<版本号>/*.jar
   ```

   例如：`drivers/starrocks/mysql-8/mysql-connector-java-8.0.17.jar`

   使用 **Bash tool** 创建目录并放置 jar 文件。如果复用已有驱动（如 MySQL 驱动），可从 `drivers/mysql/mysql-8/` 复制 jar 文件。

   **目录命名规则**：
   - 第一层目录名 = `ProductContext.name` 的值（全小写）
   - 第二层目录名 = 自定义版本标识（如 `mysql-8`、`postgresql-42.2.5`），会展示在前端供用户选择

   **jar 文件要求**：
   - 必须是自包含的（fat jar / standalone jar），无未放置的外部依赖
   - 如果驱动有多个依赖 jar，全部放入同一版本目录下
   - 不要在模块 `pom.xml` 中添加驱动依赖

7. **检查硬编码列表**

   使用 **Read tool** 检查 `ProductTypeEnum.java` 中的以下方法，评估是否需要将新类型加入：

   - **`hasDatabaseName()`**：如果新数据库的 URL 中数据库名是可选的（`[{database}]`）且希望允许不填，需将新枚举值加入排除列表
   - **`offTransactional()`**：如果新数据库不支持事务，需将其加入此方法
   - **`hasFilePath()` / `hasAddress()`**：普通关系型数据库无需修改

   普通关系型数据库通常无需修改这些方法。

8. **编译验证**

   使用 **Bash tool** 运行编译：

   ```bash
   mvn clean package -Dmaven.test.skip=true
   ```

   确认编译通过，无错误。

9. **总结**

   完成后，向用户报告：
   - 新增的枚举值名称与 id
   - 驱动目录路径与 jar 文件
   - 是否修改了硬编码列表
   - 提示："重启 manager 和 executor 服务后，前端数据源类型列表中将出现新类型。"

**输出**

```
## 数据源扩展完成

**数据库类型：** <DB_NAME>（id: <id>）
**枚举 name：** <name>
**JDBC 驱动：** <driver-class>
**驱动路径：** drivers/<name>/<version>/<jar-file>

### 修改的文件
- sqlrest-common/.../ProductTypeEnum.java — 新增枚举值
- drivers/<name>/<version>/*.jar — 新增驱动 jar

### 验证
- 编译通过 ✓

重启 manager 和 executor 服务后即可在前端使用新数据源类型。
```

**护栏**
- 开始编码前必须阅读 `docs/develop/add-relational-datasource.md` 和 `.agents/rules/java-code-style.md`
- `id` 不可与已有枚举值重复，不可修改已有枚举值的 id
- `name` 必须全小写，且必须与 `drivers/` 下的第一层目录名匹配
- `driver` 类名必须与 jar 包中实际类名一致
- `pageSql` 与 `pageConsumer` 必须配合（`?` 顺序与参数添加顺序一致）
- 驱动 jar 必须自包含，不添加为 Maven 依赖
- 协议兼容型数据库（MySQL/PG 兼容）应复用已有协议配置，不要自行编造
- 编码完成后必须运行 `mvn clean package -Dmaven.test.skip=true` 验证编译通过
- 不修改已有枚举值的任何属性，仅在列表中追加新值
