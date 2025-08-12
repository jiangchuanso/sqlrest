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
import org.dromara.sqlrest.common.util.PomVersionUtils;
import io.swagger.annotations.Api;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"存活健康接口"})
@RestController
@RequestMapping(value = Constants.MANGER_API_V1 + "/health")
public class AliveHealthController {

  @GetMapping(value = "/version", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResultEntity<String> queryFirewallRules() {
    return ResultEntity.success(PomVersionUtils.getCachedProjectVersion());
  }
}
