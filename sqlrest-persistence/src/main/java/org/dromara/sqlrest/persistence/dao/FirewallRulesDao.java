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
import org.dromara.sqlrest.common.enums.WhiteBlackEnum;
import org.dromara.sqlrest.persistence.entity.FirewallRulesEntity;
import org.dromara.sqlrest.persistence.mapper.FirewallRulesMapper;
import javax.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class FirewallRulesDao {

  private static final Long ID = 1L;

  @Resource
  private FirewallRulesMapper firewallRulesMapper;

  public FirewallRulesEntity getFirewallRules() {
    return firewallRulesMapper.selectById(ID);
  }

  public void turnOn() {
    firewallRulesMapper.updateStatus(ID, OnOffEnum.ON);
  }

  public void turnOff() {
    firewallRulesMapper.updateStatus(ID, OnOffEnum.OFF);
  }

  public void update(OnOffEnum status, WhiteBlackEnum mode, String addresses) {
    firewallRulesMapper.updateById(
        FirewallRulesEntity.builder()
            .id(ID)
            .status(status)
            .mode(mode)
            .addresses(addresses)
            .build());
  }
}
