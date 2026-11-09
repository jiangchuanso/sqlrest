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
import org.dromara.sqlrest.persistence.entity.SystemParamEntity;
import org.dromara.sqlrest.persistence.mapper.SystemParamMapper;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SystemParamDao {

  private final SystemParamMapper systemParamMapper;

  public SystemParamEntity getByParamKey(String paramKey) {
    QueryWrapper<SystemParamEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda().eq(SystemParamEntity::getParamKey, paramKey);
    return systemParamMapper.selectOne(queryWrapper);
  }

  public void updateByParamKey(String paramKey, String paramValue) {
    SystemParamEntity entity = getByParamKey(paramKey);
    if (Objects.nonNull(entity)) {
      entity.setParamValue(paramValue);
      systemParamMapper.updateById(entity);
    }
  }

}
