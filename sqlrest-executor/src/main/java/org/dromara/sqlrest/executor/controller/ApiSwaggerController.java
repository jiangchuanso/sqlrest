// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.executor.controller;

import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.models.OpenAPI;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.dromara.sqlrest.common.consts.Constants;
import org.dromara.sqlrest.core.servlet.ApiSwaggerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.spring.web.json.Json;
import springfox.documentation.spring.web.json.JsonSerializer;

@Api(tags = {"Swagger接口文档"})
@CrossOrigin
@RestController
@RequestMapping(value = Constants.API_DOC_PATH_PREFIX)
public class ApiSwaggerController {

  @Resource
  private ApiSwaggerService apiSwaggerService;
  @Resource
  private JsonSerializer jsonSerializer;

  @GetMapping(value = {"/swagger.json", "/knife4j/swagger.json"}, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Json> getSwaggerJson(HttpServletRequest request) {
    OpenAPI oas = apiSwaggerService.getSwaggerJson(request);
    return new ResponseEntity(this.jsonSerializer.toJson(oas), HttpStatus.OK);
  }

  @GetMapping(value = {"/knife4j/swagger-resources"}, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Json> getSwaggerResource(HttpServletRequest request) {
    Map<String, Object> resources = new HashMap<>();
    resources.put("name", "SQLREST在线接口文档");
    resources.put("url", "/swagger.json");
    resources.put("swaggerVersion", "3.0");
    resources.put("location", "/swagger.json");
    return new ResponseEntity(this.jsonSerializer.toJson(Lists.newArrayList(resources)), HttpStatus.OK);
  }

}
