# 新增 MCP 令牌 Tool 可见性授权开发指南

本文档阐述在 sqlrest 中为 **MCP 令牌（`SQLREST_MCP_CLIENT`）** 增加"可见 Tool 集合"授权能力所需的完整设计与实现步骤：管理端按令牌配置可见 tool，MCP 服务端在 SSE 与 StreamableHTTP 两条链路的 `tools/list` 与 `tools/call` 上按令牌身份过滤与拦截。

## 背景与目标

当前 MCP 形态：

- `SQLREST_MCP_CLIENT` 只保存 `name` / `token`，与任何 tool / group / API 无关联。
- `SQLREST_MCP_TOOL` 通过 `api_id` 关联到已上线的 API。
- manager 启动时 `McpManageService.loadMcpTools()` 把 DB 全部 tool 注册进**单例** `McpSyncServer`。
- 认证只做"令牌存在性校验"（`McpClientDao.existsAccessToken`），不携带任何权限范围。

因此任意令牌都能看到并调用全部 tool。本次改造目标：

1. 建立 **token↔tool 多对多授权模型**（`SQLREST_MCP_CLIENT_TOOL`），提供管理端配置接口与前端入口。
2. 让认证通过的**令牌身份传播**到 tool 列举/调用逻辑：SSE 会话级绑定，StreamableHTTP 请求级。
3. 在两条链路的 `tools/list` 与 `tools/call` 上按授权集合**过滤/拦截**，行为一致。
4. 未配置授权的令牌**默认全禁**（看不到、调不了任何 tool）。

### 默认语义

| 字段/条件 | 默认值 | 语义 |
|-----------|--------|------|
| 关联表无记录 | 空集合 | 该令牌 `tools/list` 返回空、`tools/call` 一律拒绝 |
| `McpToolAuthorizer` 为 `null`（SDK 默认） | 不过滤 | 保留 SDK 原生行为；manager 恒注入该 Bean，故生产环境不会出现 |

> 本次为 **BREAKING**：升级后存量令牌在完成配置前将失去全部 MCP tool 能力。文档第七节提供可选的全量回填 SQL。

---

## 一、授权模型与 DTO

### 1.1 新增关联实体 `McpClientToolEntity`

文件：`sqlrest-persistence/src/main/java/org/dromara/sqlrest/persistence/entity/McpClientToolEntity.java`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SQLREST_MCP_CLIENT_TOOL")
public class McpClientToolEntity {

  @TableId(type = IdType.AUTO)
  private Long id;

  @TableField("client_id")
  private Long clientId;

  @TableField("tool_id")
  private Long toolId;
}
```

### 1.2 新增请求 DTO `McpClientToolRequest`

文件：`sqlrest-core/src/main/java/org/dromara/sqlrest/core/dto/McpClientToolRequest.java`

```java
@Data
public class McpClientToolRequest {

  @NotNull
  @ApiModelProperty("MCP令牌ID")
  private Long clientId;

  @ApiModelProperty("授权可见的Tool ID集合(空集合表示全部禁用)")
  private List<Long> toolIds;
}
```

---

## 二、数据库迁移

新增 `V1_9_0` 迁移（MySQL 与 PostgreSQL 双份 SQL），并在 `db.changelog-master.yaml` / pg 对应 changelog 中注册。

### MySQL：`sqlrest-manager/src/main/resources/db/migration/V1_9_0__system-ddl.sql`

```sql
create table `SQLREST_MCP_CLIENT_TOOL`  (
  `id`        bigint(20)  not null auto_increment               comment '主键id',
  `client_id` bigint(20)  not null                              comment 'MCP令牌ID',
  `tool_id`   bigint(20)  not null                              comment 'MCP工具ID',
  primary key (`id`),
  unique key `uk_client_tool` (`client_id`, `tool_id`),
  FOREIGN KEY (`client_id`) REFERENCES `SQLREST_MCP_CLIENT` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`tool_id`)   REFERENCES `SQLREST_MCP_TOOL` (`id`)   ON DELETE CASCADE ON UPDATE CASCADE
) engine=InnoDB character set = utf8 comment = 'MCP令牌工具授权关联表';
```

### PostgreSQL：`sqlrest-manager/src/main/resources/pg/migration/V1_9_0__system-ddl.sql`

```sql
CREATE TABLE SQLREST_MCP_CLIENT_TOOL (
  "id"        bigserial   not null,
  "client_id" int8        not null,
  "tool_id"   int8        not null,
  primary key ("id"),
  FOREIGN KEY ("client_id") REFERENCES SQLREST_MCP_CLIENT ("id") ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY ("tool_id")   REFERENCES SQLREST_MCP_TOOL ("id")   ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE UNIQUE INDEX "SQLREST_MCP_CLIENT_TOOL_UK" ON SQLREST_MCP_CLIENT_TOOL("client_id", "tool_id");
```

> 与既有 PG DDL（`pg/migration/V1_2_2__system-ddl.sql`）保持一致：使用 `bigserial` / `int8`、
> 建表后单独创建索引、外键引用不带引号表名。

### changelog 注册

新增 `log-v1.9.0.yaml`（MySQL）与 pg 对应文件，并在两份 `db.changelog-master.yaml` 末尾 `include`：

```yaml
databaseChangeLog:
- changeSet:
    id: 1.9.0
    author: sqlrest
    runOnChange: false
    changes:
      - sqlFile:
          encoding: UTF-8
          path: db/migration/V1_9_0__system-ddl.sql
```

> **迁移只建表、不写入任何授权行**（默认全禁）。需要保持旧行为的部署请手工执行第七节的回填 SQL。

---

## 三、MCP SDK 改造（身份传播与过滤钩子）

改造集中在仓内 vendored 的 `sqlrest-mcp-core` 与 `sqlrest-mcp-springmvc`。原则：**改动最小、集中在身份字段与过滤钩子**，并在改动处注释标明为 sqlrest 定制。

### 3.1 新增过滤抽象 `McpToolAuthorizer`

文件：`sqlrest-mcp/sqlrest-mcp-core/src/main/java/io/modelcontextprotocol/server/McpToolAuthorizer.java`

```java
@FunctionalInterface
public interface McpToolAuthorizer {

  /**
   * 返回指定客户端令牌可访问的 Tool 名称集合。
   * 令牌为空或未配置授权时返回空集合（默认全禁）。
   */
  Set<String> getAllowedToolNames(String clientToken);
}
```

### 3.2 `McpServerTransport` 增加令牌读取默认方法

文件：`sqlrest-mcp/sqlrest-mcp-core/src/main/java/io/modelcontextprotocol/spec/McpServerTransport.java`

```java
public interface McpServerTransport extends McpTransport {

  /** sqlrest 定制：返回建立该传输连接时认证通过的客户端令牌，默认无。 */
  default String getClientToken() {
    return null;
  }
}
```

### 3.3 `McpServerSession` 携带令牌

文件：`sqlrest-mcp/sqlrest-mcp-core/src/main/java/io/modelcontextprotocol/spec/McpServerSession.java`

- 新增 `private final String clientToken;` 字段与构造参数；
- 新增 `public String getClientToken() { return clientToken; }`。

### 3.4 `McpAsyncServerExchange` 暴露令牌

文件：`sqlrest-mcp/sqlrest-mcp-core/src/main/java/io/modelcontextprotocol/server/McpAsyncServerExchange.java`

```java
public String getClientToken() {
  return this.session.getClientToken();
}
```

### 3.5 `McpAsyncServer` 接入过滤

文件：`sqlrest-mcp/sqlrest-mcp-core/src/main/java/io/modelcontextprotocol/server/McpAsyncServer.java`

- `McpAsyncServer` 与其内部 `AsyncServerImpl` 增加 `McpToolAuthorizer toolAuthorizer` 字段（构造参数透传）；
- `setSessionFactory` 的 lambda 从 transport 读取令牌并传入 session：

  ```java
  .setSessionFactory(transport -> new McpServerSession(
      UUID.randomUUID().toString(), transport, transport.getClientToken(),
      this::asyncInitializeRequestHandler, Mono::empty, requestHandlers, notificationHandlers));
  ```

- `toolsListRequestHandler` 按授权集合过滤：

  ```java
  return (exchange, params) -> {
    Set<String> allowed = (null == toolAuthorizer)
        ? null : toolAuthorizer.getAllowedToolNames(exchange.getClientToken());
    List<Tool> tools = this.tools.stream()
        .map(McpServerFeatures.AsyncToolSpecification::getTool)
        .filter(tool -> null == allowed || allowed.contains(tool.getName()))
        .collect(Collectors.toList());
    return Mono.just(new McpSchema.ListToolsResult(tools, null));
  };
  ```

- `toolsCallRequestHandler` 限定候选后查找；未命中按"工具不存在"处理：

  ```java
  Set<String> allowed = (null == toolAuthorizer)
      ? null : toolAuthorizer.getAllowedToolNames(exchange.getClientToken());
  Optional<AsyncToolSpecification> toolSpecification = this.tools.stream()
      .filter(tr -> null == allowed || allowed.contains(tr.getTool().getName()))
      .filter(tr -> callToolRequest.getName().equals(tr.getTool().getName()))
      .findAny();
  // 未命中：Mono.error(new McpError("Tool not found: " + callToolRequest.getName()))
  ```

> **错误码语义（重要）**：SSE 链路经 `McpServerSession.handleIncomingRequest` 的 `onErrorResume`
> （`McpServerSession.java:233-238`）会把 handler 抛出的错误**统一包装为 JSON-RPC `INTERNAL_ERROR`**；
> 而 StreamableHTTP 是自定义 handler 直接构造响应，返回 `METHOD_NOT_FOUND`。因此两条链路的错误码数值本就不同，
> 本变更**沿用现状**。统一约束是：**同一条链路上，授权过滤与"工具确实不存在"的错误 message 必须逐字一致**
> （实现上先按授权过滤候选、再走各自原生 not-found 分支，因此天然一致；SSE=`"Tool not found: <name>"`，
> StreamableHTTP 沿用其原生 `"Tool not found: '<name>' "`），禁止出现 `"Tool not allowed"` 等可区分文案，
> 以免泄露 tool 是否存在。跨链路允许文案格式不同。

> **授权仅作用于 provider 路径**：`McpServer.sync(McpServerTransportProvider)` 走 `AsyncServerImpl`，接入过滤钩子；
> deprecated 的 `McpServer.sync(ServerMcpTransport)`（内部 `LegacyAsyncServer`）不含该钩子。manager 只使用
> provider 路径，不受影响。SDK 定制处请加注释说明，避免误用。

### 3.5.1 公共过滤逻辑 `McpToolAccess`

SSE（`McpAsyncServer` 的 handler）与 StreamableHTTP（`WebMvcStreamHttpServerProvider`）共用
`io.modelcontextprotocol.server.McpToolAccess` 的静态方法：
- `resolveAllowed(authorizer, token)`：`authorizer == null` 返回 `null`（不过滤）；否则返回集合，
  `null` 归一为空集合（全禁）。
- `isAllowed(allowed, toolName)`：`allowed == null` 恒真；否则按集合判定。

两条链路都**先按授权过滤候选再走原有 not-found 分支**，因此未授权与不存在返回完全相同的错误文案。

单元测试覆盖见 `sqlrest-mcp-core` 的 `McpToolAccessTest`（null authorizer 不过滤、正常集合判定、
`null` 归一为空集合、空集合全禁）。

### 3.6 `McpServer` 构建器新增 `toolAuthorizer(...)`

文件：`sqlrest-mcp/sqlrest-mcp-core/src/main/java/io/modelcontextprotocol/server/McpServer.java`

- `AsyncSpecification` 与 `SyncSpecification` 各增加 `private McpToolAuthorizer toolAuthorizer;`、链式方法 `toolAuthorizer(McpToolAuthorizer)`；
- `build()` 时传入 `new McpAsyncServer(transportProvider, mapper, features, toolAuthorizer)`（`SyncSpecification.build()` 同样透传）。

### 3.7 SSE 传输携带令牌

文件：`sqlrest-mcp/sqlrest-mcp-springmvc/src/main/java/io/modelcontextprotocol/server/transport/WebMvcSseServerTransportProvider.java`

- 内部类 `WebMvcMcpSessionTransport` 增加 `private final String clientToken;` 构造参数，并覆写 `getClientToken()`；
- `handleSseConnection` 认证通过后，把本次校验的 `accessToken` 传入 `new WebMvcMcpSessionTransport(sessionId, sseBuilder, accessToken)`。

> SSE 的令牌只在连接建立时出现，因此绑定在会话传输对象上，经 SDK 会话工厂读取进 `McpServerSession`。

### 3.8 StreamableHTTP 传输按请求过滤

文件：`sqlrest-mcp/sqlrest-mcp-springmvc/src/main/java/io/modelcontextprotocol/server/transport/WebMvcStreamHttpServerProvider.java`

- 构造注入 `McpToolAuthorizer toolAuthorizer`；
- `handlePost` 中认证通过后，把 `accessToken` 传给 `handleListTools(id, token)` 与 `handleCallTool(id, token, params)`；
- `handleListTools` 与 `handleCallTool` 用 `toolAuthorizer.getAllowedToolNames(token)` 过滤/限定。

---

## 四、持久化层

遵循项目"双层 DAO/Mapper、无 XML"约定。

### 4.1 `McpClientToolMapper`

文件：`sqlrest-persistence/src/main/java/org/dromara/sqlrest/persistence/mapper/McpClientToolMapper.java`

```java
public interface McpClientToolMapper extends BaseMapper<McpClientToolEntity> {

  @Select("SELECT t.id AS id, t.name AS name "
      + "FROM SQLREST_MCP_CLIENT_TOOL a "
      + "JOIN SQLREST_MCP_TOOL t ON a.tool_id = t.id "
      + "WHERE a.client_id = #{clientId}")
  List<IdWithName> getAuthTool(@Param("clientId") Long clientId);

  @Select("SELECT t.name AS name "
      + "FROM SQLREST_MCP_CLIENT_TOOL a "
      + "JOIN SQLREST_MCP_CLIENT c ON a.client_id = c.id "
      + "JOIN SQLREST_MCP_TOOL t   ON a.tool_id = t.id "
      + "WHERE c.token = #{token}")
  List<String> listAllowedToolNames(@Param("token") String token);

  @Insert("<script>"
      + "INSERT INTO SQLREST_MCP_CLIENT_TOOL(client_id,tool_id) VALUES "
      + "<foreach collection='entities' item='item' separator=',' > "
      + "  ( #{item.clientId} ,#{item.toolId} ) "
      + "</foreach>"
      + "</script>")
  void insertList(@Param("entities") List<McpClientToolEntity> entities);
}
```

> `IdWithName` 位于 `sqlrest-common/src/main/java/org/dromara/sqlrest/common/dto/IdWithName.java`；`@Select` 的列别名需与实体字段对应（`name`）。PG 大小写：注解 SQL 用不带引号的大写表名，与现有 `ClientGroupMapper` 一致。

### 4.2 `McpClientToolDao`

文件：`sqlrest-persistence/src/main/java/org/dromara/sqlrest/persistence/dao/McpClientToolDao.java`

```java
@Repository
@RequiredArgsConstructor
public class McpClientToolDao {

  private final McpClientToolMapper mcpClientToolMapper;

  @Transactional(rollbackFor = Exception.class)
  public void saveAuthTool(Long clientId, List<Long> toolIds) {
    mcpClientToolMapper.delete(
        Wrappers.<McpClientToolEntity>lambdaQuery().eq(McpClientToolEntity::getClientId, clientId));
    if (CollectionUtils.isNotEmpty(toolIds)) {
      List<McpClientToolEntity> entities = toolIds.stream()
          .distinct()
          .map(toolId -> McpClientToolEntity.builder().clientId(clientId).toolId(toolId).build())
          .collect(Collectors.toList());
      mcpClientToolMapper.insertList(entities);
    }
  }

  public List<IdWithName> getAuthTool(Long clientId) {
    return mcpClientToolMapper.getAuthTool(clientId);
  }

  public List<String> listAllowedToolNames(String token) {
    if (StringUtils.isBlank(token)) {
      return Collections.emptyList();
    }
    return mcpClientToolMapper.listAllowedToolNames(token);
  }
}
```

### 4.3 `McpToolDao` 新增全量选项

文件：`sqlrest-persistence/src/main/java/org/dromara/sqlrest/persistence/dao/McpToolDao.java`

```java
public List<IdWithName> listIdName() {
  return mcpToolMapper.selectList(Wrappers.<McpToolEntity>lambdaQuery())
      .stream()
      .map(e -> IdWithName.builder().id(e.getId()).name(e.getName()).build())
      .collect(Collectors.toList());
}
```

---

## 五、Manager 服务与接口

### 5.1 `McpServerConfiguration` 注入 `McpToolAuthorizer`

文件：`sqlrest-manager/src/main/java/org/dromara/sqlrest/manager/config/McpServerConfiguration.java`

新增 Bean（**直接依赖 `McpClientToolDao`，避免 `McpManageService → McpSyncServer → authorizer → McpManageService` 的循环依赖**）：

```java
@Bean
public McpToolAuthorizer mcpToolAuthorizer(McpClientToolDao mcpClientToolDao) {
  return token -> new HashSet<>(mcpClientToolDao.listAllowedToolNames(token));
}
```

- `mcpSyncServer` 构建链增加 `.toolAuthorizer(mcpToolAuthorizer)`；
- `webMvcStreamHttpServerProvider` 构造参数增加 `mcpToolAuthorizer`。

> **不做授权结果缓存**：`mcpToolAuthorizer` 每次 `tools/list` / `tools/call` 直接查库。manager 可能以**多实例**
> 运行（同时作为 Eureka 服务端），进程内本地缓存无法跨节点失效——`saveClientToolAuth` 只清理处理请求的那个
> 节点，会造成"同一令牌在不同节点可见 tool 不同"。除非引入跨节点失效机制（广播或共享缓存），否则保持每请求
> 查库。切勿顺手加 `ConcurrentHashMap` 本地缓存。

### 5.2 `McpManageService` 新增方法

文件：`sqlrest-manager/src/main/java/org/dromara/sqlrest/manager/service/McpManageService.java`

```java
private final McpClientToolDao mcpClientToolDao;

@Transactional(rollbackFor = Exception.class)
public void saveClientToolAuth(McpClientToolRequest request) {
  if (null == mcpClientDao.getById(request.getClientId())) {
    throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_NOT_EXISTS, "clientId=" + request.getClientId());
  }
  List<Long> toolIds = null == request.getToolIds() ? Collections.emptyList() : request.getToolIds();
  for (Long toolId : toolIds) {
    if (null == mcpToolDao.getById(toolId)) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_NOT_EXISTS, "toolId=" + toolId);
    }
  }
  mcpClientToolDao.saveAuthTool(request.getClientId(), toolIds);
}

public List<IdWithName> getClientToolAuth(Long id) {
  return mcpClientToolDao.getAuthTool(id);
}

public List<IdWithName> listToolOptions() {
  return mcpToolDao.listIdName();
}
```

在 `@EventListener(ApplicationReadyEvent.class) loadMcpTools()` 末尾追加巡检日志，便于运维发现未配置令牌：

```java
long unassigned = mcpClientDao.listAll(null).stream()
    .filter(client -> mcpClientToolDao.getAuthTool(client.getId()).isEmpty())
    .count();
log.warn("There are {} MCP clients without any tool authorization.", unassigned);
```

### 5.3 `McpClientController` 新增授权接口

文件：`sqlrest-manager/src/main/java/org/dromara/sqlrest/manager/controller/McpClientController.java`

```java
@ApiOperation(value = "保存令牌授权")
@PostMapping(value = "/auth/create", produces = MediaType.APPLICATION_JSON_VALUE)
public ResultEntity createToolAuth(@Valid @RequestBody McpClientToolRequest request) {
  mcpManageService.saveClientToolAuth(request);
  return ResultEntity.success();
}

@ApiOperation(value = "回显令牌授权")
@GetMapping(value = "/auth/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
public ResultEntity<List<IdWithName>> getToolAuth(@PathVariable("id") Long id) {
  return ResultEntity.success(mcpManageService.getClientToolAuth(id));
}
```

> 返回 `ResultEntity.success(List<IdWithName>)`（元素为 `id`/`name`），与项目统一返回规范一致。

### 5.4 `McpToolController` 新增选择器接口

文件：`sqlrest-manager/src/main/java/org/dromara/sqlrest/manager/controller/McpToolController.java`

```java
@ApiOperation(value = "全部Tool选项")
@GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
public ResultEntity<List<IdWithName>> list() {
  return ResultEntity.success(mcpManageService.listToolOptions());
}
```

---

## 六、前端适配（`sqlrest-manager-ui`）

文件：`sqlrest-manager-ui/src/views/mcp/client.vue`

参考 `views/setting/client.vue` 的分组授权弹窗实现：

1. **操作列**新增“关联”按钮（文案 key `mcp.toolAuth`），点击后打开“配置 Tool”弹窗并加载：
   - 全部 tool 选项：`GET /sqlrest/manager/api/v1/mcp/tool/list`
   - 已授权回显：`GET /sqlrest/manager/api/v1/mcp/client/auth/{id}`

   > **接口契约（重要）**：本变更的三个接口均返回 `ResultEntity`：
   > - `GET /mcp/tool/list` → `ResultEntity.success(List<IdWithName>)`，前端读 `res.data.data`；
   > - `GET /mcp/client/auth/{id}` → `ResultEntity.success(List<IdWithName>)`，前端读 `res.data.data`；
   > - `POST /mcp/client/auth/create` → `ResultEntity.success()`，前端按 `res.data.code === 0` 判断成功。

2. **授权弹窗**：使用 **`el-transfer` 左右两栏穿梭框**（可搜索）绑定 `selectedToolIds` —— 左栏“可选 Tool”、
   右栏“已选 Tool”，以实现“左右两竖排多选框”的多选交互。`toolOptions` 由接口数据映射为
   `{ key: id, label: '[id] name' }`，即在 **tool 名称前显示 id**（示例：`[1] name`）：

   ```javascript
   this.toolOptions = res.data.data.map(item => ({
     key: item.id,
     label: '[' + item.id + '] ' + item.name
   }));
   ```

   `selectedToolIds` 为已授权 tool 的 id 数组（`el-transfer` 的 `v-model` 即 key 集合），保存时：

   ```javascript
   this.$http.post("/sqlrest/manager/api/v1/mcp/client/auth/create", {
     clientId: this.toolAuthClientId,
     toolIds: this.selectedToolIds
   }).then(res => { /* 成功后提示并关闭 */ });
   ```

   **布局宽度（按比例）**：`el-transfer-panel` 默认固定 `200px`，会使弹窗左右不匀称。需给弹窗设置较宽宽度
   （`width="900px"`）并用深层选择器将两栏设为**比例宽度**（各 `45%`，中间按钮区收窄），使其随弹窗自适应均分：

   ```html
   <el-dialog :visible.sync="toolAuthDialogVisible" width="900px">
     <div class="tool-auth-transfer">
       <el-transfer ...>
         <el-tooltip slot-scope="{ option }"
                     :content="option.label" placement="top"
                     :disabled="option.label.length <= 24">
           <span class="tool-auth-transfer-label">{{ option.label }}</span>
         </el-tooltip>
       </el-transfer>
     </div>
   </el-dialog>
   <style scoped>
   .tool-auth-transfer >>> .el-transfer { display: flex; align-items: center; justify-content: space-between; }
   .tool-auth-transfer >>> .el-transfer-panel { width: 45%; }
   .tool-auth-transfer >>> .el-transfer__buttons {
     display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 0 10px;
   }
   .tool-auth-transfer >>> .el-transfer-panel__item .el-checkbox__label {
     overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: calc(100% - 28px); vertical-align: middle;
   }
   .tool-auth-transfer >>> .tool-auth-transfer-label {
     display: inline-block; max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; vertical-align: middle;
   }
   </style>
   ```

   **超长名称悬浮提示**：通过 `el-transfer` 的默认作用域插槽（`slot-scope="{ option }"`）把每个选项包进
   `el-tooltip`（`:content="option.label"`），并配合 CSS `text-overflow: ellipsis` 截断；名称过长时鼠标悬浮即显示完整名称
   （`label.length <= 24` 时禁用 tooltip，避免短名称也弹提示）。

3. **i18n**：在 `sqlrest-manager-ui/src/lang/zh-CN.js` 与 `en-US.js` 的 `mcp` 段维护文案：
   `toolAuth`（按钮“关联”）、`toolAuthTitle`（弹窗标题“配置Tool”）、`toolAuthOptional` / `toolAuthSelected`
   （左右栏标题）、`toolSearchPlaceholder`、`toolAuthSaveSuccess`。

> 注意现有 `client.vue` 的 `listAll` 地址存在双斜杠笔误 `"/api/v1//mcp/client/listAll"`，新增接口请使用规范路径 `"/api/v1/mcp/..."`。

---

## 七、默认全禁与存量迁移

- 默认全禁：关联表无记录的令牌看不到、也调用不了任何 tool。
- **存量兼容（迁移内置回填）**：`V1_9_0__system-ddl.sql` 在**建表后自动执行全量回填**，为升级前已存在的
  每个令牌授权当时已配置的全部 Tool，从而保持升级前的“全量可见”行为——**升级前配置的令牌升级后可直接使用，
  无需任何手工操作**：

  ```sql
  INSERT INTO SQLREST_MCP_CLIENT_TOOL(client_id, tool_id)
  SELECT c.id, t.id
  FROM SQLREST_MCP_CLIENT c
  CROSS JOIN SQLREST_MCP_TOOL t;
  ```

  > PostgreSQL 版见 `pg/migration/V1_9_0__system-ddl.sql`（表名带双引号）。
  > 回填范围以**迁移执行时刻**的存量数据为准：迁移之后新增的 Tool 不会自动授权给既有令牌。
- **升级后新增的令牌仍默认全禁**，需在令牌管理页“配置 Tool”中显式授权。
- 如需进一步收窄存量授权，可在管理端逐个令牌调整。

回滚策略：代码回滚后 MCP 恢复全量可见；`SQLREST_MCP_CLIENT_TOOL` 表可保留（旧代码不读取），无需删表。

---

## 八、验证

### 编译

```bash
mvn clean package -Dmaven.test.skip=true
```

### 功能验证

1. 启动 manager，确认 `V1_9_0` 迁移建表、`log-v1.9.0` changelog 执行，且启动日志提示未配置授权的令牌数量。
2. **StreamableHTTP**（`/{manager}/mcp?token=...`）：
   - 令牌 A 授权 tool1：`tools/list` 仅返回 tool1；直接 `tools/call` tool2 被拒。
   - 未配置令牌 B：`tools/list` 返回空列表；调用任意 tool 被拒。
3. **SSE**（`/{manager}/mcp/sse?token=...` + `/mcp/message?sessionId=...`）：
   - 同一令牌返回与 StreamableHTTP 一致的 tool 集合；
   - 修改授权后，下一次 `tools/list` 即时生效（读取时查库过滤），已建连会话不被踢下线。
4. **安全验收（不可区分）**：对同一令牌分别调用"未授权但存在"的 tool 与"不存在"的 tool，确认**同一条链路上**
   两次返回的错误文案**逐字一致**（不可区分；跨链路允许格式不同），无 `"Tool not allowed"` 类可区分文案。
   （记录错误码：SSE 为 `INTERNAL_ERROR`、StreamableHTTP 为 `METHOD_NOT_FOUND`，属预期差异。）
5. **前端**：为令牌勾选/清空 tool 授权，回显与保存一致。
6. **回归**：为令牌授权某 tool 后，其 `tools/call` 正常执行并返回执行结果。

---

## 附：完整文件清单

| 模块 | 文件 | 操作 |
|------|------|------|
| sqlrest-mcp-core | `server/McpToolAuthorizer.java` | 新增过滤接口 |
| sqlrest-mcp-core | `server/McpToolAccess.java` | 新增公共过滤逻辑（`resolveAllowed`/`isAllowed`） |
| sqlrest-mcp-core | `src/test/java/.../server/McpToolAccessTest.java` | 新增 `McpToolAccess` 单元测试 |
| sqlrest-mcp-core | `spec/McpServerTransport.java` | 新增 `getClientToken()` 默认方法 |
| sqlrest-mcp-core | `spec/McpServerSession.java` | 新增 `clientToken` 字段与 getter |
| sqlrest-mcp-core | `server/McpAsyncServerExchange.java` | 新增 `getClientToken()` |
| sqlrest-mcp-core | `server/McpAsyncServer.java` | 构造注入 authorizer、session 令牌透传、list/call 过滤 |
| sqlrest-mcp-core | `server/McpServer.java` | `toolAuthorizer(...)` 构建方法透传 |
| sqlrest-mcp-springmvc | `transport/WebMvcSseServerTransportProvider.java` | transport 携带令牌 |
| sqlrest-mcp-springmvc | `transport/WebMvcStreamHttpServerProvider.java` | 注入 authorizer、按请求令牌过滤 |
| sqlrest-persistence | `entity/McpClientToolEntity.java` | 新增实体 |
| sqlrest-persistence | `mapper/McpClientToolMapper.java` | 新增 Mapper（join 查询 + 批量插入） |
| sqlrest-persistence | `dao/McpClientToolDao.java` | 新增 DAO（先删后插、授权查询） |
| sqlrest-persistence | `dao/McpToolDao.java` | 新增 `listIdName()` |
| sqlrest-core | `dto/McpClientToolRequest.java` | 新增请求 DTO |
| sqlrest-manager | `config/McpServerConfiguration.java` | 新增 `McpToolAuthorizer` Bean 并注入两处 |
| sqlrest-manager | `service/McpManageService.java` | 新增授权保存/回显/选项/巡检日志 |
| sqlrest-manager | `controller/McpClientController.java` | 新增 `/auth/create`、`/auth/{id}` |
| sqlrest-manager | `controller/McpToolController.java` | 新增 `/list` |
| sqlrest-manager | `resources/db/migration/V1_9_0__system-ddl.sql` | 新增迁移 |
| sqlrest-manager | `resources/pg/migration/V1_9_0__system-ddl.sql` | 新增迁移 |
| sqlrest-manager | `resources/db/changelog/log-v1.9.0.yaml`、master | changelog 注册 |
| sqlrest-manager | `resources/pg/changelog`（对应） | changelog 注册 |
| sqlrest-manager-ui | `views/mcp/client.vue` | 新增"关联"入口与 `el-transfer` 授权弹窗（tool 名称前显示 id） |
| sqlrest-manager-ui | 中/英 (`i18n`) | 新增文案 |
