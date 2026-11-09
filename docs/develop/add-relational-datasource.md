# 扩展关系型数据库数据源开发指南

本文档以新增 **STARROCKS** 类型数据源为例，阐述在 sqlrest 中扩展一种新的关系型数据库数据源所需的完整步骤。AI Agent 可参照本文档完成同类扩展开发。

> **示例背景**：StarRocks 兼容 MySQL 协议，可直接复用 MySQL JDBC 驱动（`com.mysql.jdbc.Driver`），URL 前缀为 `jdbc:mysql://`。这是一个典型的"复用已有驱动、仅新增枚举类型"的场景。

---

## 整体流程

扩展一个关系型数据库数据源只需 **两个步骤**：

| 步骤 | 操作 | 涉及文件/目录 |
|------|------|---------------|
| 1 | 在 `ProductTypeEnum` 中新增枚举值 | `sqlrest-common/src/main/java/org/dromara/sqlrest/common/enums/ProductTypeEnum.java` |
| 2 | 在 `drivers/` 目录下放置驱动 jar 文件 | `drivers/<枚举name小写>/<版本>/*.jar` |

完成上述两步后，重启服务即可在前端数据源类型列表中看到新类型，并可用于创建数据源、执行查询等操作。

---

## 步骤一：在 ProductTypeEnum 中新增枚举值

### 1.1 源文件位置

```
sqlrest-common/src/main/java/org/dromara/sqlrest/common/enums/ProductTypeEnum.java
```

### 1.2 枚举结构说明

`ProductTypeEnum` 的每个枚举值通过构建一个 `ProductContext` 对象来描述数据库类型的全部元数据。`ProductContext` 定义于：

```
sqlrest-common/src/main/java/org/dromara/sqlrest/common/dto/ProductContext.java
```

其完整属性列表如下：

| 属性 | 类型 | 说明 |
|------|------|------|
| `id` | `int` | 数据库类型的唯一数字标识，不可重复 |
| `quote` | `String` | 标识符引用符号，用于在生成 SQL 时包裹表名/列名/模式名 |
| `name` | `String` | 数据库类型的小写名称，**必须与 `drivers/` 下的目录名一致**（大小写不敏感匹配） |
| `driver` | `String` | JDBC 驱动类的全限定名，必须能从 `drivers/` 下的 jar 文件中加载到 |
| `defaultPort` | `int` | 默认端口号，用于前端展示与提示 |
| `multiDialect` | `boolean` | 是否支持多种 SQL 方言（如 OceanBase 同时兼容 MySQL/Oracle 模式），普通关系型数据库设为 `false` 或不设置 |
| `testSql` | `String` | 连接测试 SQL，为 `null` 时改用 `Connection.isValid()` 检测 |
| `urlPrefix` | `String` | JDBC URL 前缀，用于 URL 格式校验 |
| `tplUrls` | `String[]` | JDBC URL 模板数组，用于 URL 格式正则校验，支持多种格式 |
| `urlSample` | `String` | URL 示例，供前端展示参考 |
| `sqlSchemaList` | `String` | 列出所有 Schema/Database 的 SQL 语句，为 `null` 时表示不支持 |
| `retSchemaList` | `List<String>` | 硬编码的 Schema 列表（仅用于非 SQL 数据源如 SQLite、ES、HTTP） |
| `hasCatalogAndSchema` | `boolean` | 是否同时存在 Catalog 和 Schema 概念（如 SQL Server），普通数据库不设置 |
| `noViewTables` | `boolean` | 是否跳过视图列表，为 `true` 时不查询视图 |
| `adapter` | `Function<String, Pair<String, String>>` | 数据库名到 `(catalog, schema)` 的映射函数 |
| `pageSql` | `String` | 分页 SQL 模板，`%s` 为原 SQL 占位符，`?` 为参数占位符 |
| `pageConsumer` | `ThreeConsumer<Integer, Integer, List<Object>>` | 分页参数填充函数，按顺序向参数列表中添加值 |
| `executeBeforeQuery` | `Consumer<Connection>` | 查询前执行的回调（如设置会话变量），无需则为 `null` |
| `resultSetFunc` | `BiFunction<Boolean, Statement, Boolean>` | 自定义结果集处理函数（特殊数据库才需要），无需则为 `null` |

### 1.3 STARROCKS 枚举值编写示例

在 `ProductTypeEnum` 中已有的枚举值列表末尾（或合适位置）添加：

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

### 1.4 各属性编写注意事项

#### id（唯一标识）

- 取一个未被使用的整数。可通过查看已有枚举值的 `id` 来确定下一个可用值。
- 该值会通过 `DataSourceService.getTypes()` 返回给前端，作为数据库类型的数字标识。
- **注意**：不要修改已有枚举值的 `id`，否则会导致已存储的数据源记录类型错乱。

#### quote（标识符引用符号）

- MySQL 系（MySQL、MariaDB、Doris、StarRocks、GBase8A）使用反引号 `` ` ``。
- PostgreSQL 系（PostgreSQL、Kingbase、OpenGauss、Oscar、DB2、Oracle、DM）使用双引号 `"`。
- OceanBase 使用空字符串 `""`（不引用）。
- 该符号用于在生成 SQL 时包裹标识符，避免关键字冲突。需查阅目标数据库的语法手册确定。

#### name（小写名称）—— **最关键属性**

- **必须全小写**，如 `"starrocks"`。
- 此值会被 `DriverLoadService` 用于匹配 `drivers/` 下的子目录名（大小写不敏感），详见步骤二。
- 此值还用于 `ProductTypeEnum.of(name)` 方法，将字符串转换为枚举值。
- 前端展示时会通过 `type.getName().toUpperCase()` 转为大写显示。
- **注意**：个别已有枚举值的 `name` 不全小写（如 TDENGINE 的 name 为 `"TDengine"`，MongoDB 为 `"MongoDB"`），新增关系型数据库时建议统一使用全小写。

#### driver（JDBC 驱动类名）

- 必须是 jar 包中实际存在的驱动类全限定名。
- StarRocks 兼容 MySQL 协议，因此复用 MySQL 驱动 `com.mysql.jdbc.Driver`。
- 如果目标数据库有专用驱动，需填写其专用驱动类名（如 PostgreSQL 的 `org.postgresql.Driver`）。
- **注意**：驱动类名必须与 `drivers/` 下 jar 包中的实际类名完全一致，否则 `DataSourceUtils.createURLClassLoader()` 加载时会抛出 `ClassNotFoundException`。

#### defaultPort（默认端口）

- 填写数据库的默认监听端口。
- StarRocks FE 的 MySQL 协议端口默认为 9030，此处填 3306 是因为复用了 MySQL 驱动的默认配置。实际使用时用户可在前端覆盖。
- 该值仅用于展示提示，不参与连接逻辑。

#### multiDialect（多方言支持）

- 普通关系型数据库**不设置**（默认 `false`）。
- 仅当数据库同时支持多种 SQL 方言时设为 `true`（目前仅 OceanBase，它可运行在 MySQL 或 Oracle 兼容模式）。
- 设为 `true` 后，系统会在连接时执行 `testSql` 检测当前方言模式，并据此选择分页 SQL。

#### testSql（连接测试 SQL）

- 用于验证连接是否可用的 SQL 语句。
- MySQL 系使用 `"/* ping */ SELECT 1"`，其中 `/* ping */` 是 MySQL 驱动识别的特殊注释，会触发轻量级 ping，不实际执行 SQL。
- 如果数据库没有特殊的 ping 语法，使用 `"SELECT 1"` 即可。
- 如果设为 `null`，系统改用 `Connection.isValid(2)` 检测连接有效性。

#### urlPrefix（URL 前缀）

- 用于 `validJdbcUrlFormat()` 中的基础校验：`conn.getUrl().startsWith(supportDbType.getUrlPrefix())`。
- 必须与 `tplUrls` 中的前缀一致。
- StarRocks 复用 MySQL 协议，因此为 `"jdbc:mysql://"`。

#### tplUrls（URL 模板数组）

- 用于 `validJdbcUrlFormat()` 中的正则校验，URL 必须至少匹配其中一个模板。
- 模板支持的占位符：
  - `{host}` — 主机地址（必填）
  - `{port}` — 端口（`[...]` 包裹表示可选）
  - `{database}` — 数据库名（`[...]` 包裹表示可选）
  - `{params}` — 连接参数（`[...]` 包裹表示可选）
  - `{file}` — 文件路径（仅 SQLite 使用）
- 方括号 `[]` 内的部分为可选项。
- `\\?` 是转义后的问号，表示 URL 参数分隔符。
- 如果数据库支持多种 URL 格式（如 Oracle 的 SID 和 Service Name 格式），可提供多个模板。

#### urlSample（URL 示例）

- 供前端展示的完整 URL 示例，帮助用户填写正确的连接地址。
- 应包含常用参数，体现最佳实践。

#### sqlSchemaList（Schema 列表 SQL）

- 执行此 SQL 返回该数据库中所有 Schema/Database 的列表。
- MySQL 系（含 StarRocks）查询 `information_schema.SCHEMATA`。
- PostgreSQL 系查询 `information_schema.schemata`。
- Oracle 查询 `SYS.ALL_USERS`。
- 如果数据库不支持通过 SQL 列出 Schema，设为 `null`。

#### retSchemaList（硬编码 Schema 列表）

- 仅用于非 SQL 数据源（如 SQLite 返回 `["sqlite_master"]`，ES 返回 `["elasticsearch"]`，HTTP 返回 `["http"]`）。
- **关系型数据库不设置此属性**。

#### hasCatalogAndSchema（Catalog+Schema 双层结构）

- 仅 SQL Server 等同时有 Catalog 和 Schema 概念的数据库设为 `true`。
- MySQL 系只有 Database（等同于 Catalog），**不设置**（`false`）。
- 该属性影响表元数据查询时是否同时使用 catalog 和 schema。

#### noViewTables（跳过视图）

- 设为 `true` 时，系统在枚举表列表时不查询视图。
- 适用于不支持视图或视图查询有问题的数据源（如 MongoDB、TDEngine、ES、HTTP）。
- **关系型数据库通常不设置**（`false`），以支持视图查询。

#### adapter（数据库名映射函数）

- 将 JDBC URL 中的 `database` 部分映射为 `(catalog, schema)` 对。
- **MySQL 系**（MySQL、MariaDB、Doris、StarRocks、GBase8A）：`database -> Pair.of(database, null)` —— 数据库名作为 catalog，schema 为 null。
- **PostgreSQL 系**（PostgreSQL、Kingbase、OpenGauss、Oscar、Oracle、DM、DB2）：`database -> Pair.of(null, database)` —— 数据库名作为 schema，catalog 为 null。
- 如果设为 `null`，系统会通过 JDBC 元数据自动推断（`getAdapter(connection, schema)`）。
- **建议显式设置**，避免依赖 JDBC 驱动实现的元数据行为差异。

#### pageSql（分页 SQL 模板）

- 使用 `%s` 作为原始 SQL 的占位符，`?` 作为参数占位符。
- 还支持 `<LIMIT>` 占位符，会被替换为每页大小（size）的整数值（仅 SQL Server 的 TOP 语法使用）。
- 常见模式：
  - **LIMIT/OFFSET**（MySQL、PostgreSQL、StarRocks 等）：`"%s LIMIT ? OFFSET ? "`
  - **ROWNUM**（Oracle）：嵌套子查询 + ROWNUM
  - **ROW_NUMBER**（SQL Server、DB2）：窗口函数
- StarRocks 兼容 MySQL 语法，使用标准 LIMIT/OFFSET。

#### pageConsumer（分页参数填充函数）

- 与 `pageSql` 配合使用，按 `?` 的顺序向参数列表 `parameters` 中添加值。
- **LIMIT/OFFSET 模式**（参数顺序：size, offset）：
  ```java
  (page, size, parameters) -> {
      parameters.add(size);          // 第1个 ? : LIMIT size
      parameters.add((page - 1) * size);  // 第2个 ? : OFFSET (page-1)*size
  }
  ```
- **BETWEEN 模式**（DB2，参数顺序：start, end）：
  ```java
  (page, size, parameters) -> {
      parameters.add((page - 1) * size);  // BETWEEN start
      parameters.add(page * size);        // AND end
  }
  ```
- **注意**：`pageConsumer` 的参数添加顺序必须与 `pageSql` 中 `?` 的出现顺序一致，否则分页结果错误。

#### executeBeforeQuery（查询前回调）

- 在执行查询前对 Connection 设置会话变量。
- 例如 Hive/Impala 需要设置 `set hive.resultset.use.unique.column.names=false` 避免列名重复。
- 普通关系型数据库通常**不设置**（`null`）。

#### resultSetFunc（结果集处理函数）

- 用于处理特殊的多结果集行为（如 Sybase 执行分页 SQL 时会产生多个结果集，需要跳过非查询结果）。
- 普通关系型数据库通常**不设置**（`null`）。

---

## 步骤二：在 drivers 目录下放置驱动 jar 文件

### 2.1 目录结构

`drivers/` 目录的组织方式为**三层结构**：

```
drivers/
├── <枚举name小写>/          # 第一层：数据库类型目录（与 ProductContext.name 匹配）
│   └── <版本号>/            # 第二层：驱动版本目录（自定义命名，如 mysql-8）
│       └── *.jar            # 第三层：驱动 jar 文件（可放多个）
├── mysql/
│   ├── mysql-5/
│   │   └── mysql-connector-java-5.1.41.jar
│   └── mysql-8/
│       └── mysql-connector-java-8.0.17.jar
├── starrocks/
│   └── mysql-8/
│       └── mysql-connector-java-8.0.17.jar
└── ...
```

### 2.2 驱动加载机制

驱动加载由 `DriverLoadService`（`sqlrest-core` 模块）负责，核心流程如下：

1. **启动时扫描**：服务启动触发 `ApplicationReadyEvent`，调用 `DriverLoadService.loadDrivers()`。
2. **读取根路径**：从配置项 `datasource.driver.base-path`（值为环境变量 `${APP_DRIVERS_PATH}`）读取 drivers 根目录。
3. **遍历类型目录**：扫描根目录下的每个子目录，通过 `ProductTypeEnum.exists(dirName)` 判断是否为已注册的数据库类型（大小写不敏感匹配 `ProductContext.name`）。未注册的目录会被跳过。
4. **遍历版本目录**：对每个类型目录，扫描其下的版本子目录。版本目录名自定义（如 `mysql-8`、`postgresql-11.4`）。
5. **索引记录**：将 `(ProductTypeEnum, 版本名) -> 版本目录File` 的映射存入 `EnumMap`。
6. **运行时加载**：当用户创建/测试数据源时，`DataSourceUtils.createDataSource()` 通过 `JarFileClassLoader` 递归收集版本目录下所有 `.jar` 文件，创建隔离的 `URLClassLoader`，再反射加载驱动类。

> **关键配置链路**：
> `conf/config.ini` → `sqlrestctl.sh` 导出 `APP_DRIVERS_PATH` 环境变量 → `application.yaml` 中 `datasource.driver.base-path: ${APP_DRIVERS_PATH}` → `DriverLoadService.driversBasePath` 字段。

### 2.3 StarRocks 驱动放置示例

StarRocks 兼容 MySQL 协议，直接使用 MySQL JDBC 驱动：

```
drivers/
└── starrocks/                              # 对应 ProductContext.name = "starrocks"
    └── mysql-8/                            # 版本目录，命名自定义
        └── mysql-connector-java-8.0.17.jar  # 驱动 jar 文件
```

### 2.4 驱动 jar 文件注意事项

1. **无外部依赖**：放入的 jar 文件必须是**自包含的**（fat jar / standalone jar），不能依赖其他未一同放置的 jar。如果驱动有多个依赖 jar，需全部放入同一版本目录下。`JarFileClassLoader` 会递归收集目录及其子目录下的所有 `.jar` 文件。

2. **不要添加为 Maven 依赖**：`drivers/` 下的 jar 是运行时通过自定义 ClassLoader 加载的，**不是 Maven 依赖**。不要在模块的 `pom.xml` 中添加这些驱动依赖，否则会导致类冲突。

3. **目录名必须匹配枚举 name**：第一层目录名必须与 `ProductContext.name` 的值匹配（大小写不敏感）。例如 name 为 `"starrocks"`，则目录名必须为 `starrocks`（或 `StarRocks` 等，但建议统一小写）。

4. **版本目录名自定义**：第二层目录名是自定义的版本标识，会展示在前端供用户选择。建议使用有意义的命名，如 `mysql-8`、`postgresql-42.2.5`。

5. **可放置多个版本**：同一类型下可放置多个版本目录，用户在前端选择数据源时可指定使用哪个版本的驱动。例如 `mysql/` 下同时有 `mysql-5/` 和 `mysql-8/`。

6. **可放置多个 jar**：同一版本目录下可放置多个 jar 文件（驱动本身 + 依赖库），`JarFileClassLoader` 会全部加载。例如 `oracle/oracle-12c/` 下同时有 `ojdbc8-19.3.0.0.jar` 和 `orai18n-19.3.0.0.jar`。

---

## 其他注意事项

### 硬编码列表检查

`ProductTypeEnum` 中有若干方法包含硬编码的枚举值列表，新增类型时需评估是否需要将新类型加入相应列表：

#### `hasDatabaseName()`（ProductTypeEnum.java:692）

```java
public boolean hasDatabaseName() {
    return !Arrays.asList(DM, SQLITE3, MYSQL, MARIADB, GBASE8A, MONGODB, ELASTICSEARCH, HTTP).contains(this);
}
```

- 返回 `true` 时，URL 校验要求 `database` 部分不能为空。
- 如果新数据库类型的 URL 中数据库名是**可选的**（URL 模板中为 `[{database}]`）且希望允许不填，需要将新枚举值加入此排除列表。
- **STARROCKS 未加入此列表**，因此 StarRocks 的连接 URL 中必须包含数据库名。

#### `offTransactional()`（ProductTypeEnum.java:708）

```java
public boolean offTransactional() {
    return this == SYBASE || this == HIVE || this == INCEPTOR || this == IMPALA;
}
```

- 返回 `true` 时，该数据源关闭事务支持。
- 如果新数据库不支持事务（如某些 OLAP/MPP 数据库），需将其加入此方法。
- **STARROCKS 未加入**，默认开启事务。

#### `hasFilePath()` / `hasAddress()`

- `hasFilePath()` 仅 `SQLITE3` 返回 `true`（使用文件路径而非网络地址）。
- `hasAddress()` 仅 `SQLITE3` 返回 `false`。
- 普通关系型数据库无需修改这两个方法。

### 多方言数据库处理

如果新数据库支持多种 SQL 方言（如 OceanBase 同时兼容 MySQL 和 Oracle 模式），需：

1. 设置 `multiDialect(true)`。
2. 设置 `testSql` 为能检测当前方言模式的 SQL（如 OceanBase 使用 `show global variables where variable_name = 'ob_compatibility_mode'`）。
3. 参考 `PageSqlUtils.getPageSql()` 中的多方言处理逻辑，根据运行时检测结果选择对应的分页 SQL。

普通关系型数据库无需此处理。

### MySQL 协议兼容数据库

StarRocks、Doris 等兼容 MySQL 协议的数据库，可复用 MySQL 驱动，配置要点：

| 属性 | 值 | 说明 |
|------|------|------|
| `driver` | `com.mysql.jdbc.Driver` | 复用 MySQL 驱动 |
| `urlPrefix` | `jdbc:mysql://` | MySQL URL 前缀 |
| `tplUrls` | `jdbc:mysql://{host}[:{port}]/[{database}][\\?{params}]` | MySQL URL 模板 |
| `testSql` | `/* ping */ SELECT 1` | MySQL ping 语法 |
| `quote` | `` ` `` | MySQL 反引号 |
| `adapter` | `database -> Pair.of(database, null)` | 数据库名作为 catalog |
| `pageSql` | `%s LIMIT ? OFFSET ? ` | MySQL 分页语法 |
| 驱动目录 | `drivers/<name>/mysql-8/mysql-connector-java-8.0.17.jar` | 复用 MySQL 驱动 jar |

---

## 验证

完成上述两步后，按以下方式验证：

### 编译

```bash
mvn clean package -Dmaven.test.skip=true
```

### 功能验证

1. 启动 manager 和 executor 服务（参考 `sqlrestctl.sh start manager` / `sqlrestctl.sh start executor`）。
2. 查看启动日志，确认出现驱动加载日志：
   ```
   Load driver for starrocks ,version:mysql-8,path:.../drivers/starrocks/mysql-8
   Finish load jdbc drivers from local path: .../drivers
   ```
3. 调用 Manager API 查看数据源类型列表（`DataSourceService.getTypes()`），确认返回中包含 STARROCKS 类型。
4. 在前端创建 StarRocks 数据源，填写连接信息并测试连接。
5. 执行查询，验证分页功能正常。

---

## 附：完整文件清单

扩展一个关系型数据库数据源涉及的文件：

| 文件 | 操作 |
|------|------|
| `sqlrest-common/src/main/java/org/dromara/sqlrest/common/enums/ProductTypeEnum.java` | 新增枚举值 |
| `sqlrest-common/src/main/java/org/dromara/sqlrest/common/dto/ProductContext.java` | 参考（通常无需修改） |
| `drivers/<name>/<version>/*.jar` | 新增驱动目录与 jar 文件 |
| `sqlrest-core/src/main/java/org/dromara/sqlrest/core/driver/DriverLoadService.java` | 参考（通常无需修改） |
| `sqlrest-core/src/main/java/org/dromara/sqlrest/core/util/DataSourceUtils.java` | 参考（通常无需修改） |

> 正常情况下，扩展关系型数据库数据源**只需修改 `ProductTypeEnum.java` 和 `drivers/` 目录**，无需改动其他代码。
