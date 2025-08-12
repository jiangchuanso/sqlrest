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
import org.dromara.sqlrest.persistence.entity.ApiContextEntity;
import org.dromara.sqlrest.persistence.mapper.ApiContextMapper;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ApiContextDao {

  @Resource
  private ApiContextMapper apiContextMapper;

  @Transactional(rollbackFor = Exception.class)
  public void batchInsert(List<ApiContextEntity> records) {
    if (null != records && records.size() > 0) {
      records.forEach(apiContextMapper::insert);
    }
  }

  public List<ApiContextEntity> getByApiConfigId(Long apiId) {
    QueryWrapper<ApiContextEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda().eq(ApiContextEntity::getApiId, apiId);
    return apiContextMapper.selectList(queryWrapper);
  }

  public void deleteByApiConfigId(Long apiId) {
    QueryWrapper<ApiContextEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda().eq(ApiContextEntity::getApiId, apiId);
    apiContextMapper.delete(queryWrapper);
  }

}
