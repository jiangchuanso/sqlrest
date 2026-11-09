# Service 层约定

> 本文档描述 Service 类的编写约定。

## 类设计

- **Service 是具体的 `@Service` 类，不使用接口 + Impl 模式。**
- 不创建接口，不使用 `Impl` 后缀。
- 示例：`sqlrest-core/.../service/DataSourceService.java:54-55`

```java
@Service
@RequiredArgsConstructor
public class DataSourceService {
```

- 需要日志的 Service 添加 `@Slf4j`。
- 示例：`ApiAssignmentService.java:94`

## 依赖注入

- 使用 `@RequiredArgsConstructor` 构造器注入，字段用 `private final` 修饰。
- **禁止使用 `@Resource`**，**禁止使用 `@Autowired`**，**禁止字段注入**。
- 示例：`DataSourceService.java`

```java
@Service
@RequiredArgsConstructor
public class DataSourceService {

  private final DataSourceDao dataSourceDao;
}
```

- 跨 Bean 查找时可使用 `SpringUtil.getBean(...)`（示例：`DataSourceService.java:125,184,187`）。

## 事务

- 使用 `@Transactional(rollbackFor = Exception.class)`，**必须指定 `rollbackFor = Exception.class`**。
- 事务注解可同时出现在 Service 和 DAO 上。
- 示例：`ApiAssignmentService.java:607,644`、`ApiAssignmentDao.java:43,53,132`

```java
@Transactional(rollbackFor = Exception.class)
public void createDataSource(DataSourceRequest request) {
  ...
}
```

## DTO/Entity 转换

- 使用 Hutool 的 `BeanUtil.copyProperties` 进行 DTO 与 Entity 互转，**不使用 MapStruct**。
- 示例：`DataSourceService.java:80,92`

```java
DataSourceEntity entity = BeanUtil.copyProperties(request, DataSourceEntity.class);
```

## 分页

- 使用 `PageUtils.getPage(Supplier<List<T>>, page, size)` 模式。
- 示例：`DataSourceService.java:74-86,194-203`

## 异常抛出

- 业务异常使用 `CommonException`，传入 `ResponseErrorCode` 和 i18n 消息 key。
- 示例：`DataSourceService.java:140`

```java
throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_NOT_EXISTS, "common.id.not.found", id);
```

详见 `exception-and-i18n.md`。
