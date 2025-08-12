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
import org.dromara.sqlrest.persistence.entity.SystemUserEntity;
import org.dromara.sqlrest.persistence.mapper.SystemUserMapper;
import java.util.Objects;
import javax.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class SystemUserDao {

  @Resource
  private SystemUserMapper systemUserMapper;

  public SystemUserEntity getById(Long id) {
    return systemUserMapper.selectById(id);
  }

  public SystemUserEntity findByUsername(String username) {
    QueryWrapper<SystemUserEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda().eq(SystemUserEntity::getUsername, username);
    return systemUserMapper.selectOne(queryWrapper);
  }

  public void updateUserPassword(String username, String newPassword) {
    SystemUserEntity userEntity = findByUsername(username);
    if (Objects.nonNull(userEntity)) {
      userEntity.setPassword(newPassword);
      systemUserMapper.updateById(userEntity);
    }
  }

}
