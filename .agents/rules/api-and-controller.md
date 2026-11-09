# API 与 Controller 约定

> 本文档描述 REST Controller、统一返回、URL 规范的约定。

## Controller 类约定

类级注解顺序（示例 `ApiAssignmentController.java`、`DataSourceController.java`）：

```java
@Api(tags = {"数据源管理接口"})
@RestController
@RequestMapping(value = Constants.MANGER_API_V1 + "/datasource")
public class DataSourceController {
```

约束：
- **必须使用 `@RestController`**。
- **Swagger 注解使用 Springfox 2**（`io.swagger.annotations.*`），不是 OpenAPI 3 / Springdoc。
  - `@Api(tags = {...})` 类级
  - `@ApiOperation(value = "...")` 方法级
  - `@ApiModelProperty("...")` 字段级
- **Swagger 文案使用中文**。

## URL 路径规范

- 管理 API 使用 `Constants.MANGER_API_V1` 前缀 = `/sqlrest/manager/api/v1`。
  - 示例：`Constants.MANGER_API_V1 + "/datasource"` → `/sqlrest/manager/api/v1/datasource`
- **注意：常量名 `MANGER_API_V1` 是既有的拼写错误（应为 MANAGER），但已被全代码库使用，必须沿用此名称，不要"修正"它。**
- 执行器 API 使用更简短的路径，如 `/token`、`/api/...`。

## 方法级映射

```java
@ApiOperation(value = "获取自动提示列表")
@GetMapping(value = "/completions", produces = MediaType.APPLICATION_JSON_VALUE)
public ResultEntity completions() {
  return ResultEntity.success(apiAssignmentService.completions());
}
```

约束：
- **每个映射注解都必须包含 `produces = MediaType.APPLICATION_JSON_VALUE`**。
- 创建和更新均使用 **POST**（更新不用 PUT）。
- 分页查询返回 `PageResult<T>`，其余返回 `ResultEntity`。

## 统一返回对象

### ResultEntity（单对象/操作结果）

位于 `sqlrest-common/.../dto/ResultEntity.java`：

```java
public class ResultEntity<T> implements Serializable {
  private Integer code;    // 0 = 成功
  private String message;  // "success" 或错误描述
  private T data;
}
```

- **成功码是 `0`，不是 `200`。**
- 静态工厂方法：
  - `ResultEntity.success()` — code=0, message="success", data=null
  - `ResultEntity.success(T data)` — code=0, message="success", data=data
  - `ResultEntity.failed(ResponseErrorCode code)`
  - `ResultEntity.failed(ResponseErrorCode code, String message)`
  - `ResultEntity.failed(String message)`

### PageResult（分页列表）

位于 `sqlrest-common/.../dto/PageResult.java`：

```java
public class PageResult<E> implements Serializable {
  private Integer code = 0;
  private String message = "success";
  private Pagination pagination;  // 嵌套: page, total, size
  private List<E> data;
}
```

- 分页接口直接返回 `PageResult<T>`，**不要再用 `ResultEntity` 包裹**。

## 参数与校验

- POST 请求体使用 `@Valid @RequestBody XxxRequest request`。
- 路径参数使用 `@PathVariable("id") Long id`。
- 查询参数使用 `@RequestParam`。
- 校验注解使用 `javax.validation`（`@NotNull`、`@NotBlank` 等）。

## 依赖注入

- Controller 使用 `@RequiredArgsConstructor` 构造器注入，字段用 `private final` 修饰。
- **禁止使用 `@Resource`**，**禁止使用 `@Autowired`**。
- 示例：`DataSourceController.java`

```java
@Api(tags = {"数据源管理接口"})
@RestController
@RequestMapping(value = Constants.MANGER_API_V1 + "/datasource")
@RequiredArgsConstructor
public class DataSourceController {

  private final DataSourceService datasourceService;
}
```
