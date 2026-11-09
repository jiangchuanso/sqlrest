# Java 代码风格规范

> 本文档基于代码库实际约定归纳，编写 Java 代码时必须遵守。

## 缩进与格式

- **统一使用 2 空格缩进**，不使用 4 空格或 Tab。
- 示例：`sqlrest-common/.../dto/ResultEntity.java`、`sqlrest-core/.../service/DataSourceService.java`

## 版权头

- 每个 Java 文件开头应包含以下版权头（9 行）：

```java
// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
///////////////////////////////////////////////////////////
```

- 新建文件时 `Date` 改为当前日期，其余保持不变。

## Lombok 使用

- **广泛使用 Lombok**，优先选用以下注解：
  - 数据类：`@Data`（或 `@Getter` + `@Setter`）
  - 日志：`@Slf4j`（不要手写 `LoggerFactory.getLogger(...)`）
  - 构造器：`@Builder`、`@NoArgsConstructor`、`@AllArgsConstructor`
  - 异常处理：`@SneakyThrows`
- 枚举类使用 `@Getter`（不用 `@Data`）。
- 示例：`sqlrest-persistence/.../entity/DataSourceEntity.java:27-30`

## 注释与 Swagger 语言

- **Swagger 描述、`@ApiModelProperty`、`@Api`、`@ApiOperation` 的文案均使用中文**。
- 代码内业务注释也使用中文。
- 示例：`@Api(tags = {"数据源管理接口"})`、`@ApiOperation(value = "获取自动提示列表")`

## 导入顺序

- 导入未严格排序，但大致遵循：`java.*` → `javax.*` → 第三方库 → `org.dromara.*` → Spring。
- 无 checkstyle 强制，但应保持同文件内一致。
- **禁止使用通配符导入**（`import xxx.*`），逐个导入类。

## 依赖注入

- 使用 **`@RequiredArgsConstructor` 构造器注入**，注入的字段必须用 `private final` 修饰。
- **禁止使用 `@Resource`**，**禁止使用 `@Autowired`**，**禁止字段注入**。
- 示例：`sqlrest-core/.../service/DataSourceService.java`

```java
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DataSourceService {

  private final DataSourceDao dataSourceDao;
}
```

- 对于需要 `@Value` 注入的非 Bean 配置字段，保持 `@Value` 字段注入，**不加 `final`**（`@RequiredArgsConstructor` 只处理 `final` 字段）。
- 对于匿名内部类中无法使用构造器注入的场景，通过 `@Bean` 方法参数传入依赖（示例：`AuthenticationConfiguration.java`）。

## 日志

- 使用 Lombok `@Slf4j`，通过 `log.info(...)`、`log.error(...)` 输出日志。
- 每个 Spring Boot 服务有独立的 Logback 配置：
  - `sqlrest-manager/src/main/resources/logback-manager.xml`
  - `sqlrest-executor/src/main/resources/logback-executor.xml`
  - `sqlrest-gateway/src/main/resources/logback-gateway.xml`
- 日志格式：`[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%-5level] [%thread] [%logger{50}] - %msg%n`
- 日志写入 `./logs/<service>_server.log`，按天 + 512MB 滚动，保留 30 天。
