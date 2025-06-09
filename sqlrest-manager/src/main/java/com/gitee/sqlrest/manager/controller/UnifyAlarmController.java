// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.sqlrest.manager.controller;

import com.gitee.sqlrest.common.consts.Constants;
import com.gitee.sqlrest.common.dto.ResultEntity;
import com.gitee.sqlrest.core.dto.NameValueBaseResponse;
import com.gitee.sqlrest.core.dto.TestAlarmConfigRequest;
import com.gitee.sqlrest.core.dto.UpdateAlarmConfigRequest;
import com.gitee.sqlrest.core.executor.UnifyAlarmOpsService;
import com.gitee.sqlrest.persistence.entity.UnifyAlarmEntity;
import io.swagger.annotations.Api;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"告警配置接口"})
@RestController
@RequestMapping(value = Constants.MANGER_API_V1 + "/alarm")
public class UnifyAlarmController {

  @Resource
  private UnifyAlarmOpsService unifyAlarmOpsService;

  @GetMapping(value = "/detail", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResultEntity<UnifyAlarmEntity> getUnifyAlarmConfig() {
    return ResultEntity.success(unifyAlarmOpsService.getUnifyAlarmConfig());
  }

  @GetMapping(value = "/example", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResultEntity<List<NameValueBaseResponse>> getExampleDataModel() {
    return ResultEntity.success(unifyAlarmOpsService.getExampleDataModel());
  }

  @PostMapping(value = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResultEntity testUnifyAlarmConfig(@Valid @RequestBody TestAlarmConfigRequest request) {
    unifyAlarmOpsService.testUnifyAlarmConfig(request);
    return ResultEntity.success();
  }

  @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResultEntity saveUnifyAlarmConfig(@Valid @RequestBody UpdateAlarmConfigRequest request) {
    unifyAlarmOpsService.updateUnifyAlarmConfig(request);
    return ResultEntity.success();
  }
}
