# 持久层与 MyBatis 约定

> 本文档描述 DAO/Mapper 分层、SQL 编写、实体映射的约定。

## 双层设计（关键约束）

持久层采用**两层设计**，Service 注入 DAO 而非 Mapper：

1. **Mapper** = MyBatis-Plus 接口，继承 `BaseMapper<Entity>`。
   - 包路径：`org.dromara.sqlrest.persistence.mapper`
   - 命名：`XxxMapper`
   - 通过 `@MapperScan("org.dromara.sqlrest.persistence.mapper")` 扫描
   - 示例：`DataSourceMapper.java:18`

2. **DAO** = `@Repository` 类，封装 Mapper。
   - 包路径：`org.dromara.sqlrest.persistence.dao`
   - 命名：`XxxDao`
   - Service 注入 DAO，不直接注入 Mapper
   - 示例：`DataSourceDao.java:24-28`

```java
@Repository
@RequiredArgsConstructor
public class DataSourceDao {
  private final DataSourceMapper dataSourceMapper;
}
```

**新增持久层代码时必须遵循此两层模式。**

## SQL 编写方式

**没有 XML Mapper 文件**——`sqlrest-persistence` 模块没有 `resources` 目录。SQL 通过以下方式编写：

1. **MyBatis-Plus API**（CRUD 操作）：
   - `Wrappers.<Entity>lambdaQuery()...`
   - `QueryWrapper`
   - `selectById`、`insert`、`updateById`、`deleteById`
   - 示例：`DataSourceDao.java:38-50`

2. **注解 SQL**（复杂查询）：
   - 使用 `@Select`、`@Update`、`@Delete` 注解
   - 动态 SQL 使用 `<script>` 标签
   - 示例：`AccessRecordMapper.java:25-219`

## 多数据库 SQL 分支（关键约束）

项目同时支持 MySQL 和 PostgreSQL。当 SQL 在两者间存在差异时，必须使用 `_databaseId` 条件分支：

```java
@Select("<script>"
  + "<if test='_databaseId == \"mysql\" '>"
  + "  SELECT ... FROM DUAL"
  + "</if>"
  + "<if test='_databaseId == \"postgresql\" '>"
  + "  SELECT ..."
  + "</if>"
  + "</script>")
List<Xxx> querySomething();
```

- `_databaseId` 由 `MybatisDatabaseConfiguration` 中的 `VendorDatabaseIdProvider` Bean 提供。
- **新增有数据库差异的 SQL 时必须使用此模式。**

## 参数绑定

- 使用 `#{param}` 进行安全参数绑定。
- 仅在需要字面量替换时使用 `${param}`（如 `LIMIT ${limit}`、`INTERVAL ${days} DAY`）。
- 示例：`AccessRecordMapper.java:49,53`

## 实体约定

示例 `DataSourceEntity.java:27-32`：

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@TableName(value = "SQLREST_DATASOURCE", autoResultMap = true)
public class DataSourceEntity {
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;
  @TableField(value = "type", typeHandler = EnumTypeHandler.class)
  private ProductTypeEnum type;
}
```

约束：
- **表名全大写**，前缀 `SQLREST_`（如 `SQLREST_DATASOURCE`、`SQLREST_API_ASSIGNMENT`）。
- **枚举字段使用 `EnumTypeHandler`** 持久化。
- **JSON 字段使用自定义 TypeHandler**（如 `PoolConfigHandler`），放在 `persistence.handler` 包。
- **`create_time`/`update_time` 字段使用 `FieldStrategy.NEVER`** 防止更新时覆盖。
- **时间戳使用 `java.sql.Timestamp`**，不用 `LocalDateTime`。
- 实体类使用 `@Data @Builder @NoArgsConstructor @AllArgsConstructor`。

## 数据库迁移（Liquibase）

Manager 启动时执行 Liquibase 迁移。Changelog 和 SQL 位于 `sqlrest-manager/src/main/resources/`：

- MySQL：`db/changelog/` + `db/migration/V*__*.sql`
- PostgreSQL：`pg/changelog/` + `pg/migration/V*__*.sql`

自定义 changelog 表名：`SQLREST_DB_CHANGE_LOG_RECORD` / `SQLREST_DB_CHANGE_LOG_LOCK`。

新增 schema 变更时：
1. 追加新的 `log-vX.Y.Z.yaml` changelog 文件
2. 在 `db.changelog-master.yaml` 中注册

**MySQL 和 PostgreSQL 的迁移脚本必须分别维护，保持结构同步。**
