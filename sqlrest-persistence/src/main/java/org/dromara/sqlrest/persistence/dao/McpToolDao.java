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
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.dromara.sqlrest.common.dto.IdWithName;
import org.dromara.sqlrest.persistence.entity.McpToolEntity;
import org.dromara.sqlrest.persistence.mapper.McpToolMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class McpToolDao {

  private final McpToolMapper mcpToolMapper;

  public void insert(McpToolEntity entity) {
    mcpToolMapper.insert(entity);
  }

  public McpToolEntity getById(Long id) {
    return mcpToolMapper.selectById(id);
  }

  public List<McpToolEntity> listAll(String searchText) {
    return mcpToolMapper.selectList(
        Wrappers.<McpToolEntity>lambdaQuery()
            .like(StringUtils.isNotBlank(searchText), McpToolEntity::getName, searchText)
            .orderByDesc(McpToolEntity::getCreateTime)
    );
  }

  public List<IdWithName> listIdName() {
    return mcpToolMapper.selectList(Wrappers.<McpToolEntity>lambdaQuery())
        .stream()
        .map(e -> IdWithName.builder().id(e.getId()).name(e.getName()).build())
        .collect(Collectors.toList());
  }

  public void updateById(McpToolEntity entity) {
    mcpToolMapper.updateById(entity);
  }

  public void deleteById(Long id) {
    mcpToolMapper.deleteById(id);
  }
}
