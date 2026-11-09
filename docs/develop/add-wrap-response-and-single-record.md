# 新增响应包装与单条返回配置开发指南

本文档阐述在 sqlrest 中为 **API 接口响应**新增两个布尔配置项 `wrapResponse`（响应是否包装为系统标准格式）与 `singleRecord`（响应是否为单条记录）所需的完整设计与实现步骤。

## 背景与目标

当前系统通过 `ApiAssignmentEntity.responseFormat`（`Map<DataTypeFormatEnum,String>`）中的
`DataTypeFormatEnum.USE_SYSTEM_RESPONSE_FORMAT` 枚举键（值为 `"false"`/`"true"` 字符串）来控制线上接口返回
完整 `ResultEntity`（含 `code/message/data`）还是只返回 `data` 部分。

本次改造目标：

1. 将 `USE_SYSTEM_RESPONSE_FORMAT` 从 `formatMap` 中独立成实体字段 `Boolean wrapResponse`，并提供独立默认值。
2. 新增实体字段 `Boolean singleRecord`，控制接口响应是单条还是多条：单条时返回 `list` 首个元素，否则保留原状。
3. 前端两个开关放置在**接口输出格式**属性页（label 为 `common2.outputFormat`，tab `name="outputParams"`）中。

### 默认值语义（与现有逻辑等价）

| 字段 | 默认值 | 现有逻辑等价语义 |
|------|--------|------------------|
| `wrapResponse` | `true` | 现 `!"false".equals(useSystemValue)`，非 `"false"` 即返回完整 `ResultEntity`。默认即 `true` |
| `singleRecord` | `false` | 现有逻辑不折叠 `list`，保持原样返回。即 `false` |

---

## 一、实体与 DTO 字段变更（数据模型）

### 1.1 `ApiAssignmentEntity` 新增字段

文件：`sqlrest-persistence/src/main/java/org/dromara/sqlrest/persistence/entity/ApiAssignmentEntity.java`

在 `response_format` 字段定义（当前 `86-87` 行）之后新增：

```java
@TableField("wrap_response")
private Boolean wrapResponse;

@TableField("single_record")
private Boolean singleRecord;
```

### 1.2 `ApiDebugExecuteRequest` 新增字段

`sqlrest-core/src/main/java/org/dromara/sqlrest/core/dto/ApiDebugExecuteRequest.java`（当前 `36-37` 行 `formatMap` 之后）：

```java
@ApiModelProperty("响应是否包装为系统标准格式")
private Boolean wrapResponse;

@ApiModelProperty("响应是否为单条记录(单条时取list首个元素)")
private Boolean singleRecord;
```

### 1.3 `ApiAssignmentSaveRequest` 新增字段

`sqlrest-core/src/main/java/org/dromara/sqlrest/core/dto/ApiAssignmentSaveRequest.java`（当前 `90` 行 `formatMap` 之后）：

```java
@ApiModelProperty("响应是否包装为系统标准格式")
private Boolean wrapResponse;

@ApiModelProperty("响应是否为单条记录(单条时取list首个元素)")
private Boolean singleRecord;
```

### 1.4 `ApiAssignmentDetailResponse` 新增字段

`sqlrest-core/src/main/java/org/dromara/sqlrest/core/dto/ApiAssignmentDetailResponse.java`：当前 `formatMap` 字段（54 行）之后新增，回显时字段通过 `BeanUtil.copyProperties` 自动拷贝，仅显式补充注释：

```java
@ApiModelProperty("响应是否包装为系统标准格式")
private Boolean wrapResponse;

@ApiModelProperty("响应是否为单条记录(单条时取list首个元素)")
private Boolean singleRecord;
```

### 1.5 `ExpImpAssignmentModel` 新增字段（导入导出）

`sqlrest-core/src/main/java/org/dromara/sqlrest/core/dto/ExpImpAssignmentModel.java`（当前 `responseFormat` 52 行之后）：

```java
private Boolean wrapResponse;

private Boolean singleRecord;
```

---

## 二、数据库迁移

新增 `V1_8_1` 迁移（MySQL 与 PostgreSQL 双份 SQL），并在 `db.changelog-master.yaml` / pg 对应 changelog 中注册。

### MySQL：`sqlrest-manager/src/main/resources/db/migration/V1_8_1__system-ddl.sql`

```sql
ALTER TABLE `SQLREST_API_ASSIGNMENT`
  ADD COLUMN `wrap_response` tinyint(1) NOT NULL DEFAULT 1 COMMENT '接口响应是否包装为系统标准格式' AFTER `response_format`;
ALTER TABLE `SQLREST_API_ASSIGNMENT`
  ADD COLUMN `single_record` tinyint(1) NOT NULL DEFAULT 0 COMMENT '接口响应是否为单条记录' AFTER `wrap_response`;
```

### PostgreSQL：`sqlrest-manager/src/main/resources/pg/migration/V1_8_1__system-ddl.sql`

```sql
ALTER TABLE "SQLREST_API_ASSIGNMENT" ADD COLUMN "wrap_response" boolean NOT NULL DEFAULT true;
ALTER TABLE "SQLREST_API_ASSIGNMENT" ADD COLUMN "single_record" boolean NOT NULL DEFAULT false;
```

### changelog 注册

参考现有 `log-v1.7.1.yaml`，新增 `log-v1.8.1.yaml` 并在 `db.changelog-master.yaml`、pg 对应 master 中注册。

---

## 三、服务层适配

### 3.1 `ApiAssignmentService` 保存/更新/详情

`sqlrest-core/src/main/java/org/dromara/sqlrest/core/service/ApiAssignmentService.java`：

**`createAssignment`（当前 `449-450` 行）** 在 `setResponseFormat` 后新增：

```java
assignmentEntity.setWrapResponse(request.getWrapResponse());
assignmentEntity.setSingleRecord(request.getSingleRecord());
```

**`updateAssignment`（当前 `544-545` 行）** 同样新增上述两行。

**`buildAssignmentDetail`（当前 `565-567` 行）**：`BeanUtil.copyProperties` 已自动拷贝新增字段到 `ApiAssignmentDetailResponse`，无需额外处理。

### 3.2 `ApiExecuteService.doExecute`：`singleRecord` 折叠

`sqlrest-core/src/main/java/org/dromara/sqlrest/core/exec/ApiExecuteService.java`

`doExecute` 当前 `133-140` 行在处理完单/多条及 HTTP 特判（`134-136` 行）后，新增 `singleRecord` 折叠逻辑（`137-140` 行）：

```java
Object answer = results.size() > 1 ? results : results.stream().findAny().orElse(null);
if (ProductTypeEnum.HTTP == dsEntity.getType() && answer instanceof List) {
  answer = ((List) answer).get(0);
}
if (Boolean.TRUE.equals(config.getSingleRecord()) && answer instanceof List) {
  List<?> list = (List<?>) answer;
  answer = list.isEmpty() ? null : list.get(0);
}
return ResultEntity.success(answer);
```

> 折叠逻辑放在 `execute` 执行结果构建处，线上 HTTP（`HttpApiServlet`）与 MCP 工具（`McpToolCallHandler`）两条数据流出点同时生效。

### 3.3 `HttpApiServlet.process`：`wrapResponse` 读取替换

`sqlrest-executor/src/main/java/org/dromara/sqlrest/executor/model/HttpApiServlet.java`

当前 `38-49` 行从 `responseFormat.get(USE_SYSTEM_RESPONSE_FORMAT)` 读取。改为：

```java
boolean useSystemFormat = !Boolean.FALSE.equals(apiConfigEntity.getWrapResponse());

String json;
if (useSystemFormat) {
  json = JacksonUtils.toJsonStr(result, apiConfigEntity.getResponseFormat());
} else {
  json = JacksonUtils.toJsonStr(result.getData(), apiConfigEntity.getResponseFormat());
}
response.getWriter().append(json);
```

### 3.4 `ApiDebugExecuteRequest` 调试接口（`debug` 方法）

`ApiAssignmentService.debugExecute`（方法自 `169` 行起）：

- 响应构建处（约 `347` 行）：`entity = ResultEntity.success(respMap)` 不变。
- 序列化返回处（约 `363-368` 行）**始终输出完整 `ResultEntity`**（`response.getWriter().append(JacksonUtils.toJsonStr(entity, formatMap))`），
  即调试页不做 `wrapResponse` 剥离——`wrapResponse` 仅控制线上 HTTP 接口（`HttpApiServlet`）的响应格式，
  调试响应固定包含 `code/message` 及 `{answer,logs,types}` 数据，便于在调试页观察执行日志。
- `singleRecord` 折叠：调试执行在 `339-342` 行（紧跟 `336-338` 行 HTTP 特判）计算 `answer` 后执行：
  ```java
  if (Boolean.TRUE.equals(request.getSingleRecord()) && answer instanceof List) {
    List<?> list = (List<?>) answer;
    answer = list.isEmpty() ? null : list.get(0);
  }
  ```

### 3.5 `ApiSwaggerService`（Swagger schema 生成）

`sqlrest-core/src/main/java/org/dromara/sqlrest/core/servlet/ApiSwaggerService.java`

**wrapResponse 读取替换**（`getApiResponses`，原 `350-356` 行读取 `USE_SYSTEM_RESPONSE_FORMAT`，现为 `350` 行）。改为：

```java
boolean useSystemFormat = !Boolean.FALSE.equals(assignment.getWrapResponse());
```

**singleRecord 影响 data schema**：`outputs` 描述 `data` 的单条对象结构。运行时 `singleRecord=true` 时 `data`
折叠为单对象，`false` 时保持数组。为让 OpenAPI 文档与实际返回一致，新增 helper：

```java
private Schema wrapDataBySingleRecord(Schema itemSchema, ApiAssignmentEntity assignment) {
  if (Boolean.TRUE.equals(assignment.getSingleRecord())) {
    return itemSchema;                            // true → 单对象
  }
  return new ArraySchema().items(itemSchema);     // false → 数组(list包裹单对象)
}
```

两处应用（helper `wrapDataBySingleRecord` 定义于 `484-489` 行）：
- `wrapResponse=true`（`401` 行）：`rootSchema.addProperties("data", wrapDataBySingleRecord(dataSchema, assignment))`
- `wrapResponse=false`（`452-455` 行）：整个根 schema 即 data，`rootSchema = wrapDataBySingleRecord(rootSchema, assignment)`

### 3.6 导入导出（`ExportImportService`）

- **导出**（`120-121` 行 `setResponseFormat` 之后）：新增
  ```java
  model.setWrapResponse(assignmentEntity.getWrapResponse());
  model.setSingleRecord(assignmentEntity.getSingleRecord());
  ```
  导出时不再需要从 `responseFormat` 中剔除 `USE_SYSTEM_RESPONSE_FORMAT` 键：该键已由启动升级器
  （`ResponseUpgradeRunner`，见 4.2）从存量 `response_format` 中剥离，且枚举删除后新保存的数据不会写入该键。

- **导入**（`toApiAssignment`，`177-178` 行 `setFormatMap` 之后）：新增
  ```java
  request.setWrapResponse(model.getWrapResponse());
  request.setSingleRecord(model.getSingleRecord());
  ```

---

## 四、`DataTypeFormatEnum` 变更与存量升级

### 4.1 删除枚举值

`sqlrest-common/src/main/java/org/dromara/sqlrest/common/enums/DataTypeFormatEnum.java`

删除第 `27` 行的 `USE_SYSTEM_RESPONSE_FORMAT` 枚举值，以及其专属三参构造器（当前 `40-44` 行）与 `remarkKey` 字段（`32` 行）、`getRemark` 中的相关分支，保持其余枚举构造器不变。

### 4.2 存量数据升级（`ResponseUpgradeRunner`）

**问题**：删除 `USE_SYSTEM_RESPONSE_FORMAT` 枚举后，`FormatMapHandler.string2map`（`FormatMapHandler.java:58`）使用
`JsonUtils.toBeanType(str, new TypeReference<Map<DataTypeFormatEnum, String>>(){})` 反序列化
`response_format` 时，遇到旧数据中遗留的 `"USE_SYSTEM_RESPONSE_FORMAT"` 键会导致 JsonMappingException
（未知枚举值）。因此存量数据必须在**删除枚举的同时**从 `response_format` 中剥离该键，并回填到新的 `wrap_response` 列。

**方案**：在 `sqlrest-manager/src/main/java/org/dromara/sqlrest/manager/upgrade/` 下，基于现有
`VersionUpgradeRunner`（`ApplicationRunner` + `@Transactional`）模式新增 `ResponseUpgradeRunner`，
启动时执行一次。

#### 4.2.1 关键约束：必须用字符串级 JSON 处理，不能用枚举反序列化

由于枚举已被删除，升级逻辑中**绝不能**再调用 `Map<DataTypeFormatEnum,String>` 反序列化（否则升级器自身
就会因旧数据抛异常）。必须使用**通用字符串键映射**处理 JSON，即读取原始的 `response_format` 文本，用
`Map<String,String>`（或树节点）解析、剥离、再写回。

#### 4.2.2 新增 Mapper 原始读取方法

`sqlrest-persistence/.../mapper/ApiAssignmentMapper.java` 新增一个原生 SQL 方法，直接返回 `id` 与
`response_format` 原始文本（避免走 `FormatMapHandler` 反序列化，也从根上绕开枚举问题）：

```java
@Select("SELECT id AS id, response_format AS response_format "
    + "FROM SQLREST_API_ASSIGNMENT")
List<ResponseFormatRawRow> selectResponseFormatRaw();
```

`ResponseFormatRawRow` 为轻量实体（`id` Long、`responseFormat` String），定义于 `sqlrest-persistence/.../entity/`，
仅保留 `@TableId`/`@TableField("response_format")` 映射。

#### 4.2.3 新增 Mapper 更新方法（CAS 条件更新，保证幂等）

采用**乐观锁（CAS）条件更新**：仅在目标行 `response_format` 仍等于升级器读取到的旧值时，才执行更新。
`UPDATE` 影响行数为 0 表示该行已被并发修改（用户在升级期间编辑保存），此时**跳过、不覆盖用户最新数据**。
这是保证幂等、避免覆盖用户在升级窗口期写入新数据的关键：

```java
@Update("UPDATE SQLREST_API_ASSIGNMENT "
        + "SET wrap_response = #{wrapResponse}, response_format = #{responseFormat} "
        + "WHERE id = #{id} AND response_format = #{oldResponseFormat}")
int updateWrapResponseIfUnchanged(@Param("id") Long id,
                                  @Param("wrapResponse") Boolean wrapResponse,
                                  @Param("responseFormat") String responseFormat,
                                  @Param("oldResponseFormat") String oldResponseFormat);
```

> **CAS 语义**：`WHERE response_format = #{oldResponseFormat}` 使更新成为原子、条件式。并发编辑该行会导致 `response_format` 变化，`UPDATE` 影响 0 行，升级器不改这些行，从而保证不丢失用户数据。

#### 4.2.4 升级器实现（`ResponseUpgradeRunner`）

```
sqlrest-manager/src/main/java/org/dromara/sqlrest/manager/upgrade/ResponseUpgradeRunner.java
```

```java
@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class ResponseUpgradeRunner implements ApplicationRunner {

  private static final String LEGACY_KEY = "USE_SYSTEM_RESPONSE_FORMAT";
  private static final String RESPONSE_FORMAT_FIELD = "responseFormat";
  private static final String WRAP_RESPONSE_FIELD = "wrapResponse";
  private static final String SINGLE_RECORD_FIELD = "singleRecord";
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final ApiAssignmentMapper apiAssignmentMapper;
  private final VersionCommitMapper versionCommitMapper;
  private final ApiOnlineMapper apiOnlineMapper;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void run(ApplicationArguments args) {
    log.info("Start upgrade legacy USE_SYSTEM_RESPONSE_FORMAT config now.");
    StopWatch stopWatch = new StopWatch();
    stopWatch.start("UpgradeResponse");
    int updated = upgradeAssignments();
    if (updated > 0) {
      // 仅当 assignment 表确有升级时，才继续处理版本快照与在线快照
      int updatedVcsCount = upgradeVersionCommitContents();
      log.info("upgrade vcs data total count: {}.",updatedVcsCount);
      int updatedOnlineCount = upgradeOnlineContents();
      log.info("upgrade online data total count: {}.",updatedOnlineCount);
    }
    stopWatch.stop();
    log.info(
        "Success upgrade legacy USE_SYSTEM_RESPONSE_FORMAT config for {} APIs, total cost {} ms .",
        updated,
        stopWatch.getTotalTimeMillis());
  }

  private int upgradeAssignments() {
    int updated = 0;
    List<ResponseFormatRawRow> rows = apiAssignmentMapper.selectResponseFormatRaw();
    for (ResponseFormatRawRow row : rows) {
      // 幂等规则1：无需处理的行（空 format、不含旧键、非法 JSON）一律跳过，不产生任何写操作
      if (StringUtils.isBlank(row.getResponseFormat())) {
        continue;
      }
      String oldRaw = row.getResponseFormat();
      Map<String, Object> map;
      try {
        map = OBJECT_MAPPER.readValue(oldRaw, new TypeReference<Map<String, Object>>() {});
      } catch (Exception e) {
        log.warn("Skip upgrade assignment[id={}] for invalid response_format: {}", row.getId(),
            e.getMessage());
        continue;
      }
      // 幂等规则2：不含旧键的行直接跳过，结果不受重复运行影响
      if (!map.containsKey(LEGACY_KEY)) {
        continue;
      }
      Object legacy = map.remove(LEGACY_KEY);
      Boolean wrap = !"false".equalsIgnoreCase(String.valueOf(legacy));
      String newFmt = JsonUtils.toJsonString(map);
      // 幂等规则3：CAS 条件更新，仅当 response_format 未被并发修改时才写入；
      // 返回影响行数，0 表示行已被用户并发编辑，跳过（不覆盖用户最新数据）
      int affected = apiAssignmentMapper.updateWrapResponseIfUnchanged(row.getId(), wrap, newFmt, oldRaw);
      if (affected > 0) {
        updated++;
      } else {
        log.info("Skip upgrade assignment[id={}] for concurrent modification (CAS miss).", row.getId());
      }
    }
    return updated;
  }
}
```

> `upgradeVersionCommitContents()` / `upgradeOnlineContents()` 遍历 `SQLREST_VERSION_COMMIT` 与
> `SQLREST_API_ONLINE` 全表，将 `content` 经 `upgradeContentJson`（见 4.2.7）处理后 `updateById` 写回，
> 实现见源文件 `119-153` 行。

> 说明：
> - 升级在 manager 的 `ApplicationRunner` 阶段执行（早于 `run()/httpServlet` 读取线上接口配置），`scanBasePackages` 已含 `org.dromara.sqlrest.manager`，自动被扫描注册；`@Order(1)` 保证优先执行。
> - **隔离设计**：升级逻辑自包含在独立 `ResponseUpgradeRunner` 中，不依赖 `VersionCommitDao` 内容快照，仅需 `ApiAssignmentMapper`、`VersionCommitMapper`、`ApiOnlineMapper`，无需改动原 `VersionUpgradeRunner`。
> - **事务**：整体一个事务，任一失败则回滚，保证 `wrap_response` 与 `response_format` 两列一致性；CAS 更新 + 事务共同保障遇到异常时不产生部分写。

#### 4.2.5 幂等性保证

升级逻辑在**三次运行、多节点并发、以及升级与在线编辑并发**三种场景下均收敛到一致结果，不会重复处理、不会写脏：

| 场景 | 幂等保障机制 | 结果 |
|------|--------------|------|
| **单节点重复运行/重启** | 已升级行 `response_format` 不再含 `LEGACY_KEY`，`map.containsKey(LEGACY_KEY)` 为 `false` 直接 `continue`，零写操作 | 重复运行无副作用 |
| **多 manager 节点同时启动** | 每个节点独立执行 CAS 更新；幂等规则 1/2 过滤后，仅剩未升级行；多个线程 `UPDATE ... WHERE id=? AND response_format=?`，仅一个命中（首个），后续 CAS miss 影响 0 行被跳过 | 结果唯一、不重复写 |
| **升级与在线编辑并发** | CAS 乐观锁（`WHERE response_format = #{oldResponseFormat}`）：用户保存导致 `response_format` 变化后，升级器 `UPDATE` 影响 0 行 → 跳过，**不覆盖用户最新数据** | 用户数据不丢失，未升级行待下次启动处理 |

> **设计要点**：
> - `updateWrapResponseIfUnchanged` 返回 `int` 影响行数，是判断 CAS 命中/未命中的依据；未命中（影响 0 行）一律跳过，绝不用陈旧读取覆盖新数据。
> - 这里刻意**不用** `SELECT ... FOR UPDATE` 或全局分布式锁：本升级目标是"自然幂等"，CAS 已能保证不丢数据，同时避免对 `SQLREST_API_ASSIGNMENT` 加行锁影响启动期在线写入。
> - 升级是**最终一致**收敛：任何未完成升级的行（并发跳过）会在下一次 manager 启动时自然处理，无需人工干预。

#### 4.2.6 升级与读取的时序

- 升级器在 manager 启动时完成写库；executor/gateway 通过缓存读取最新配置。需保证**删除枚举逻辑与升级器同一次发布**，且升级器先于任何包含旧键数据的反序列化链路执行，避免生成时抛枚举异常。
- `single_record` 列由迁移 `DEFAULT 0` 提供，升级阶段无需回填（沿用现有逻辑 false 默认值）。

#### 4.2.7 快照表 Content 一并升级

除 `SQLREST_API_ASSIGNMENT.response_format` 外，`SQLREST_VERSION_COMMIT.content` 与 `SQLREST_API_ONLINE.content`
保存了整个实体（含 `responseFormat`）的 JSON 快照。这些快照会在部署/回滚（`ApiAssignmentService.deployAssignment`）
与在线执行（`ApiOnlineDao.buildAssignmentEntity` → `getByApiId`）中被反序列化为 `ApiAssignmentEntity`；
若 `responseFormat` 仍含旧键，会在枚举删除后抛 `JsonMappingException`，破坏线上接口与版本列表。

```java
// ResponseUpgradeRunner 内：用 Jackson 树模型（ObjectNode）处理 content
private String upgradeContentJson(String content) {
  ObjectNode root;
  try {
    JsonNode tree = OBJECT_MAPPER.readTree(content);
    if (!(tree instanceof ObjectNode)) {
      return null;
    }
    root = (ObjectNode) tree;
  } catch (JsonProcessingException e) {
    log.warn("Skip upgrade content for invalid json: {}", e.getMessage());
    return null;
  }
  JsonNode fmtNode = root.get(RESPONSE_FORMAT_FIELD);
  if (null == fmtNode || !(fmtNode instanceof ObjectNode)) {
    return null;
  }
  ObjectNode fmt = (ObjectNode) fmtNode;
  if (!fmt.has(LEGACY_KEY)) {
    return null; // 无旧键，幂等跳过
  }
  JsonNode legacy = fmt.remove(LEGACY_KEY);
  boolean wrap = !"false".equalsIgnoreCase(legacy.asText());
  root.put(WRAP_RESPONSE_FIELD, wrap);
  root.put(SINGLE_RECORD_FIELD, false);
  return root.toString();
}
```

- `VERSION_COMMIT.content` / `API_ONLINE.content` 统一经 `upgradeContentJson` 处理：剥离 `responseFormat` 旧键、
  推导写入顶层 `wrapResponse` 并置 `singleRecord=false`。
- **执行门控**：仅当 `upgradeAssignments()` 返回 `updated > 0`（assignment 表确有行被升级）时才会执行
  快照表升级（`ResponseUpgradeRunner.run` 中 `if (updated > 0)` 分支）；assignment 表无需升级时跳过全表扫描。
- 幂等：树解析后无旧键（`fmt.has(LEGACY_KEY)` 为 `false`）返回 `null`，`updateById` 不触发。
- 该处理使用纯 JSON 树模型，**不触发**枚举反序列化，且不影响表结构迁移。

---

## 五、前端适配（`sqlrest-manager-ui`）

文件：`sqlrest-manager-ui/src/views/interface/common.vue`

在**接口输出格式** tab 页（label 为 `common2.outputFormat`，tab `name="outputParams"`，当前 `456` 行）中，`formatMap` 区域（`485-502` 行）之后新增两个开关：

### 5.1 模板

在 `formatMap` 的 `el-form-item` 之后追加（各自包在独立 `el-row`/`el-col` 中，`wrapResponse` 带 tooltip 说明）：

```html
<el-row>
  <el-col :span="24">
    <el-form-item prop="wrapResponse" label-width="120px">
      <span slot="label" style="display:inline-block;">
        {{ $t('common2.wrapResponse') }}
        <el-tooltip effect="dark"
                    :content="$t('common2.wrapResponseTip')"
                    placement="bottom">
          <i class='el-icon-question' />
        </el-tooltip>
      </span>
      <el-switch v-model="createParam.wrapResponse"
                 :active-value="true"
                 :inactive-value="false"
                 :disabled="isOnlyShowDetail"/>
    </el-form-item>
  </el-col>
</el-row>
<el-row>
  <el-col :span="24">
    <el-form-item :label="$t('common2.singleRecord')"
                  prop="singleRecord"
                  label-width="120px">
      <el-switch v-model="createParam.singleRecord"
                 :active-value="true"
                 :inactive-value="false"
                 :disabled="isOnlyShowDetail"/>
    </el-form-item>
  </el-col>
</el-row>
```

同时，`formatMap` 列表不再包含 `USE_SYSTEM_RESPONSE_FORMAT`（后端枚举删除后，`/response-type-format` 已不返回该键，前端列表自动去除）。为兼容旧数据，`mergeFormatMap`（`1195-1221` 行）中做了防御性过滤与合并：先 `filter(item => item.key !== 'USE_SYSTEM_RESPONSE_FORMAT')` 剔除旧键，旧值为空时直接取最新配置列表，否则以最新配置为底、用清洗后的旧值按 key 覆盖合并。

### 5.2 `createParam` 初始值（`852-869` 行）

```javascript
formatMap: null,
wrapResponse: true,
singleRecord: false,
```

### 5.3 详情回显（`applyAssignmentDetail`，`1000-1021` 行）

```javascript
wrapResponse: (detail.wrapResponse !== undefined && detail.wrapResponse !== null)
  ? detail.wrapResponse : true,
singleRecord: (detail.singleRecord !== undefined && detail.singleRecord !== null)
  ? detail.singleRecord : false,
```

### 5.4 提交体（triple 提交点）

- 创建（`1672-1673` 行）、更新（`1725-1726` 行）、调试（`1843-1844` 行）三处 `data` 均追加：

```javascript
wrapResponse: this.createParam.wrapResponse,
singleRecord: this.createParam.singleRecord,
```

> 调试请求 `ApiDebugExecuteRequest` 同样新增了这两个字段，因此 debug 提交体也要带上
>（注意：调试响应固定返回完整 `ResultEntity`，`wrapResponse` 仅影响线上接口，见 3.4）。

### 5.5 i18n

在 `common2` 语言包（`sqlrest-manager-ui/src/lang/zh-CN.js` 与 `en-US.js`，`682-684` 行）中新增
`wrapResponse`、`wrapResponseTip`、`singleRecord` 三个文案：

- 中文：`wrapResponse: '响应格式包装'`、`wrapResponseTip: '系统返回格式配置：启用时返回完整ResultEntity，关闭时只返回data属性'`、`singleRecord: '响应单条记录'`
- 英文：`wrapResponse: 'Wrap response as system format'`、`wrapResponseTip: 'System response format config: enabled returns full ResultEntity, disabled returns only data property'`、`singleRecord: 'Return single record'`

---

## 六、验证

### 编译

```bash
mvn clean package -Dmaven.test.skip=true
```

### 功能验证

1. 启动 manager（执行 `V1_8_1` 迁移并触发 `ResponseUpgradeRunner` 升级）。
2. 确认启动日志成功升级旧存 `USE_SYSTEM_RESPONSE_FORMAT` 数据。
3. 创建/编辑接口，在**接口输出格式** tab 中设置：
   - 关闭 `wrapResponse`：访问接口应只返回 `data`（不含 `code/message`）。
   - 开启 `singleRecord`：当 SQL 返回单条元素时，`data` 为首元素对象而非数组。
4. 调试接口：调试响应固定为完整 `ResultEntity`（`wrapResponse` 不影响调试输出）；校验 `singleRecord=true` 时 `answer` 为 `list` 首元素。
5. 导出 → 导入：`wrapResponse`/`singleRecord` 配置往返一致。
6. 上线后调用真实接口，校验两开关行为与预期一致。

---

## 附：完整文件清单

| 模块 | 文件 | 操作 |
|------|------|------|
| sqlrest-common | `common/enums/DataTypeFormatEnum.java` | 删除 `USE_SYSTEM_RESPONSE_FORMAT` |
| sqlrest-persistence | `persistence/entity/ApiAssignmentEntity.java` | 新增 `wrapResponse`、`singleRecord` 字段 |
| sqlrest-core | `core/dto/ApiDebugExecuteRequest.java` | 新增两个字段 |
| sqlrest-core | `core/dto/ApiAssignmentSaveRequest.java` | 新增两个字段 |
| sqlrest-core | `core/dto/ApiAssignmentDetailResponse.java` | 新增两个字段 |
| sqlrest-core | `core/dto/ExpImpAssignmentModel.java` | 新增两个字段 |
| sqlrest-core | `core/service/ApiAssignmentService.java` | 保存/更新/调试适配 |
| sqlrest-core | `core/exec/ApiExecuteService.java` | `singleRecord` 折叠 |
| sqlrest-executor | `executor/model/HttpApiServlet.java` | `wrapResponse` 读取替换 |
| sqlrest-core | `core/servlet/ApiSwaggerService.java` | `wrapResponse` 读取替换 + `data` schema 按 `singleRecord` 决定数组/单对象（新增 `wrapDataBySingleRecord`） |
| sqlrest-core | `core/service/ExportImportService.java` | 导入/导出字段适配 |
| sqlrest-persistence | `persistence/mapper/ApiAssignmentMapper.java` | 新增 `selectResponseFormatRaw`、`updateWrapResponseIfUnchanged` 方法 |
| sqlrest-persistence | `persistence/entity/ResponseFormatRawRow.java` | 新增原始 `response_format` 读取实体 |
| sqlrest-manager | `manager/upgrade/ResponseUpgradeRunner.java` | 存量数据升级（assignment 表 + 版本/在线快照表） |
| sqlrest-manager | `resources/db/migration/V1_8_1__system-ddl.sql` | 新增迁移 |
| sqlrest-manager | `resources/pg/migration/V1_8_1__system-ddl.sql` | 新增迁移 |
| sqlrest-manager | `resources/db/changelog/log-v1.8.1.yaml`、master | changelog 注册 |
| sqlrest-manager | `resources/pg/changelog`（对应） | changelog 注册 |
| sqlrest-manager-ui | `views/interface/common.vue` | 接口响应 tab 新增两个开关 |
| sqlrest-manager-ui | 中/英 (`i18n`) | 新增文案 |