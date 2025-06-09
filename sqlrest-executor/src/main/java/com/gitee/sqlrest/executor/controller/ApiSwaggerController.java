// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.sqlrest.executor.controller;

import com.gitee.sqlrest.common.consts.Constants;
import com.gitee.sqlrest.core.servlet.ApiSwaggerService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.models.OpenAPI;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.spring.web.json.Json;
import springfox.documentation.spring.web.json.JsonSerializer;

@Api(tags = {"Swagger接口文档"})
@RestController
@RequestMapping(value = Constants.API_DOC_PATH_PREFIX)
public class ApiSwaggerController {

  @Resource
  private ApiSwaggerService apiSwaggerService;
  @Resource
  private JsonSerializer jsonSerializer;

  @GetMapping(value = "/swagger.json", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Json> getSwaggerJson(HttpServletRequest request) {
    OpenAPI oas = apiSwaggerService.getSwaggerJson(request);
    return new ResponseEntity(this.jsonSerializer.toJson(oas), HttpStatus.OK);
  }
}
