// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.gateway;

import org.dromara.sqlrest.common.enums.OnOffEnum;
import org.dromara.sqlrest.common.enums.WhiteBlackEnum;
import org.dromara.sqlrest.core.dto.UpdateFirewallRulesRequest;
import org.dromara.sqlrest.persistence.dao.FirewallRulesDao;
import org.dromara.sqlrest.persistence.entity.FirewallRulesEntity;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FirewallFilterService {

  private volatile FirewallRulesEntity firewallRules;

  @Resource
  private FirewallRulesDao firewallRulesDao;

  @PostConstruct
  public void init() {
    try {
      refresh();
    } catch (Exception e) {
      log.warn("load firewall rules failed:{}", e.getMessage());
    }
  }

  public void refresh() {
    this.firewallRules = firewallRulesDao.getFirewallRules();
  }

  public boolean canAccess(String address) {
    if (null == this.firewallRules) {
      refresh();
    }

    if (OnOffEnum.OFF.equals(firewallRules.getStatus())) {
      return true;
    }

    String lists = Optional.ofNullable(firewallRules.getAddresses()).orElse(Strings.EMPTY);
    Set<String> addressSets = Arrays.asList(lists.split("\n"))
        .stream().map(t -> t.trim())
        .filter(t -> StringUtils.isNotBlank(t))
        .collect(Collectors.toSet());
    if (WhiteBlackEnum.WHITE.equals(firewallRules.getMode())) {
      return addressSets.contains(address);
    } else if (WhiteBlackEnum.BLACK.equals(firewallRules.getMode())) {
      return !addressSets.contains(address);
    } else {
      return false;
    }
  }

  public FirewallRulesEntity getFirewallRules() {
    return this.firewallRules;
  }

  public void updateFirewallRules(UpdateFirewallRulesRequest request) {
    firewallRulesDao.update(request.getStatus(), request.getMode(), request.getAddresses());
    this.refresh();
  }

}
