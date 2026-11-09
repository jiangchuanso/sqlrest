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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.dromara.sqlrest.persistence.entity.McpClientEntity;
import org.dromara.sqlrest.persistence.mapper.McpClientMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class McpClientDao {

  private final McpClientMapper mcpClientMapper;

  public void insert(McpClientEntity mcpClientEntity) {
    mcpClientMapper.insert(mcpClientEntity);
  }

  public McpClientEntity getById(Long id) {
    return mcpClientMapper.selectById(id);
  }

  public List<McpClientEntity> listAll(String searchText) {
    return mcpClientMapper.selectList(
        Wrappers.<McpClientEntity>lambdaQuery()
            .like(StringUtils.isNotBlank(searchText), McpClientEntity::getName, searchText)
            .orderByDesc(McpClientEntity::getCreateTime)
    );
  }

  public boolean existsAccessToken(String accessToken) {
    QueryWrapper<McpClientEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda().eq(McpClientEntity::getToken, accessToken);
    return null != mcpClientMapper.selectOne(queryWrapper);
  }

  public void updateById(McpClientEntity entity) {
    mcpClientMapper.updateById(entity);
  }

  public void deleteById(Long id) {
    mcpClientMapper.deleteById(id);
  }

}
