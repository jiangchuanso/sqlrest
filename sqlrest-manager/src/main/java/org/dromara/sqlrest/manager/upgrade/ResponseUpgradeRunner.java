// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.manager.upgrade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.sqlrest.persistence.entity.ApiOnlineEntity;
import org.dromara.sqlrest.persistence.entity.ResponseFormatRawRow;
import org.dromara.sqlrest.persistence.entity.VersionCommitEntity;
import org.dromara.sqlrest.persistence.mapper.ApiAssignmentMapper;
import org.dromara.sqlrest.persistence.mapper.ApiOnlineMapper;
import org.dromara.sqlrest.persistence.mapper.VersionCommitMapper;
import org.dromara.sqlrest.persistence.util.JsonUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

/**
 * 升级存量响应格式配置。
 *
 * <p>删除 DataTypeFormatEnum.USE_SYSTEM_RESPONSE_FORMAT 枚举后，数据库中遗留的
 * "USE_SYSTEM_RESPONSE_FORMAT" 键将无法被反序列化。此升级器在启动时将存量该键的值迁移到
 * 新的 wrap_response 配置，并从以下位置中剥离该键：
 * <ul>
 *   <li>SQLREST_API_ASSIGNMENT.response_format</li>
 *   <li>SQLREST_VERSION_COMMIT.content（完整实体快照的 responseFormat 字段）</li>
 *   <li>SQLREST_API_ONLINE.content（完整实体快照的 responseFormat 字段）</li>
 * </ul>
 *
 * <p>采用 CAS（条件更新）以保证幂等：仅当数据未被并发修改时写入，否则跳过，
 * 不覆盖用户在升级窗口保存的数据。
 */
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
      if (!map.containsKey(LEGACY_KEY)) {
        continue;
      }
      Object legacy = map.remove(LEGACY_KEY);
      Boolean wrap = !"false".equalsIgnoreCase(String.valueOf(legacy));
      String newFmt = JsonUtils.toJsonString(map);
      int affected = apiAssignmentMapper.updateWrapResponseIfUnchanged(row.getId(), wrap, newFmt, oldRaw);
      if (affected > 0) {
        updated++;
      } else {
        log.info("Skip upgrade assignment[id={}] for concurrent modification (CAS miss).", row.getId());
      }
    }
    return updated;
  }

  private int upgradeVersionCommitContents() {
    int updated = 0;
    log.info("Start upgrade vcs data now... ");
    List<VersionCommitEntity> commits = versionCommitMapper.selectList(null);
    for (VersionCommitEntity commit : commits) {
      if (StringUtils.isBlank(commit.getContent())) {
        continue;
      }
      String newContent = upgradeContentJson(commit.getContent());
      if (newContent != null) {
        commit.setContent(newContent);
        versionCommitMapper.updateById(commit);
        updated++;
      }
    }
    return updated;
  }

  private int upgradeOnlineContents() {
    int updated = 0;
    log.info("Start upgrade online data now... ");
    List<ApiOnlineEntity> onlines = apiOnlineMapper.selectList(null);
    for (ApiOnlineEntity online : onlines) {
      if (StringUtils.isBlank(online.getContent())) {
        continue;
      }
      String newContent = upgradeContentJson(online.getContent());
      if (newContent != null) {
        online.setContent(newContent);
        apiOnlineMapper.updateById(online);
        updated++;
      }
    }
    return updated;
  }

  /**
   * 使用 Jackson 树模型处理接口实体快照 JSON：
   * 将 responseFormat 中的 USE_SYSTEM_RESPONSE_FORMAT 键移除，并把其值推导的 wrapResponse
   * 写入顶层，singleRecord 置为 false。无旧键时返回 null（无需变更）。
   */
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
      return null;
    }
    JsonNode legacy = fmt.remove(LEGACY_KEY);
    boolean wrap = !"false".equalsIgnoreCase(legacy.asText());
    root.put(WRAP_RESPONSE_FIELD, wrap);
    root.put(SINGLE_RECORD_FIELD, false);
    return root.toString();
  }
}