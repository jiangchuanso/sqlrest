// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.manager.controller;

import org.dromara.sqlrest.common.consts.Constants;
import org.dromara.sqlrest.common.dto.ResultEntity;
import org.dromara.sqlrest.core.dto.UpdateFirewallRulesRequest;
import org.dromara.sqlrest.core.gateway.FirewallFilterService;
import org.dromara.sqlrest.persistence.entity.FirewallRulesEntity;
import io.swagger.annotations.Api;
import javax.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"防火墙管理接口"})
@RestController
@RequestMapping(value = Constants.MANGER_API_V1 + "/firewall")
public class FirewallController {

  @Resource
  private FirewallFilterService firewallFilterService;
  
  @GetMapping(value = "/detail", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResultEntity<FirewallRulesEntity> queryFirewallRules() {
    return ResultEntity.success(firewallFilterService.getFirewallRules());
  }

  @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResultEntity saveFirewallRules(@RequestBody UpdateFirewallRulesRequest request) {
    firewallFilterService.updateFirewallRules(request);
    return ResultEntity.success();
  }

}
