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

import org.dromara.sqlrest.common.enums.OnOffEnum;
import org.dromara.sqlrest.persistence.entity.UnifyAlarmEntity;
import org.dromara.sqlrest.persistence.mapper.UnifyAlarmMapper;
import javax.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class UnifyAlarmDao {

  private static final Long ID = 1L;

  @Resource
  private UnifyAlarmMapper unifyAlarmMapper;

  public UnifyAlarmEntity getUnifyAlarmConfig() {
    return unifyAlarmMapper.selectById(ID);
  }

  public void turnOn() {
    unifyAlarmMapper.updateStatus(ID, OnOffEnum.ON);
  }

  public void turnOff() {
    unifyAlarmMapper.updateStatus(ID, OnOffEnum.OFF);
  }

  public void update(OnOffEnum status, String endpoint, String contentType, String inputTemplate) {
    unifyAlarmMapper.updateById(
        UnifyAlarmEntity.builder()
            .id(ID)
            .status(status)
            .endpoint(endpoint)
            .contentType(contentType)
            .inputTemplate(inputTemplate)
            .build());
  }
}
