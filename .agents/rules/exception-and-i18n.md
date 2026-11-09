# 异常处理与国际化约定

> 本文档描述全局异常处理、自定义异常、错误码与 i18n 的约定。

## 全局异常处理

位于 `sqlrest-manager/.../exception/ExceptionController.java`：

```java
@Slf4j
@ControllerAdvice
public class ExceptionController {
  @ResponseBody
  @ExceptionHandler(value = {MethodArgumentNotValidException.class})
  public ResultEntity argumentValidException(MethodArgumentNotValidException e) { ... }

  @ResponseBody
  @ExceptionHandler(value = Exception.class)
  public ResultEntity errorHandler(Exception e) { ... }
}
```

约束：
- 使用 `@ControllerAdvice` + `@ResponseBody`（不是 `@RestControllerAdvice`）。
- 校验异常 (`MethodArgumentNotValidException`) 单独处理，错误信息用 `; ` 拼接，映射到 `ERROR_INVALID_ARGUMENT`。
- `CommonException` 特殊处理，提取其 `ResponseErrorCode`。
- 其余异常统一兜底处理。

## 自定义异常

### CommonException

位于 `sqlrest-common/.../exception/CommonException.java`：

```java
@Data
public class CommonException extends RuntimeException {
  private ResponseErrorCode code;
  public CommonException(ResponseErrorCode code, String messageKey, Object... args) {
    super(I18nUtils.getMessage(messageKey, args));
    this.code = code;
  }
}
```

**关键约束：构造函数接收的是 i18n 消息 key（如 `"common.id.not.found"`），不是字面消息字符串。** 消息通过 `I18nUtils.getMessage()` 解析。

抛出示例：
```java
throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_NOT_EXISTS, "common.id.not.found", id);
```

### 其他异常

- `UnAuthorizedException` — 未授权（401）
- `UnPermissionException` — 无权限（403）
- 均位于 `org.dromara.sqlrest.common.exception`

## 错误码

位于 `sqlrest-common/.../exception/ResponseErrorCode.java`，枚举类型：

| 码值 | 含义 |
|---|---|
| `0` | 成功 |
| `1-11` | 内部错误 |
| `401` | 未授权 |
| `403` | 无权限 |
| `404` | 资源不存在 |
| `429` | 限流 |

- 枚举使用 `@Getter`（不用 `@Data`）。
- `getMessage()` 解析 i18n key `exception.<ENUM_NAME>`。
- 示例：`ResponseErrorCode.java:44-47`

## 国际化 (i18n)

- 消息资源文件位于各服务的 `src/main/resources/i18n/messages*.properties`。
- `application.yaml` 中配置：`spring.messages.basename: i18n/messages`。
- `I18nUtils` 工具类负责消息解析。
- **所有面向用户的错误消息必须通过 i18n key 引用，不要硬编码中文字符串到异常消息中。**

## 常量与枚举

### Constants

位于 `sqlrest-common/.../consts/Constants.java`：

- 是一个 **abstract class**（不是 interface），包含 `public static final` 字段。
- 存放 URL 前缀、Header 名称、缓存名称、Token 有效期、MCP 端点等。
- **`MANGER_API_V1` 是既有拼写错误，全代码库使用此名称，不要修正。**

### 枚举

位于 `sqlrest-common/.../enums/`：

- 使用 `@Getter`。
- 简单枚举：构造器带字段，手写 getter，提供静态 `exists()` 查询。
- 富枚举（如 `ProductTypeEnum`）：每个常量携带 DB 相关的 SQL/方言/分页逻辑，提供静态 `of(String)` 和 `exists(String)` 工厂方法。
- 示例：`ProductTypeEnum.java:749,739`
