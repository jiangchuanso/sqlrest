// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.persistence.dao;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dromara.sqlrest.common.dto.IdWithName;
import org.dromara.sqlrest.persistence.entity.McpClientToolEntity;
import org.dromara.sqlrest.persistence.mapper.McpClientToolMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
