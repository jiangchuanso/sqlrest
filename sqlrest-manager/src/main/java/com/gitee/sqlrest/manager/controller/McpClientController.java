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
import com.gitee.sqlrest.common.dto.PageResult;
import com.gitee.sqlrest.common.dto.ResultEntity;
import com.gitee.sqlrest.core.dto.EntitySearchRequest;
import com.gitee.sqlrest.manager.service.McpManageService;
import com.gitee.sqlrest.persistence.entity.McpClientEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"MCP令牌管理接口"})
@RestController
@RequestMapping(value = Constants.MANGER_API_V1 + "/mcp/client")
public class McpClientController {

  @Resource
  private McpManageService mcpManageService;

  @ApiOperation(value = "获取MCP服务地址")
  @GetMapping(value = "/endpoint", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResultEntity<String> getMcpSseEndpoint() {
    return ResultEntity.success(mcpManageService.getMcpSseEndpoint());
  }

  @ApiOperation(value = "添加令牌")
  @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResultEntity create(@Valid @NotBlank(message = "name不能为空") @RequestParam("name") String name) {
    mcpManageService.createClient(name);
    return ResultEntity.success();
  }

  @ApiOperation(value = "更新令牌")
  @PostMapping(value = "/update/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResultEntity update(@PathVariable("id") Long id,
      @Valid @NotBlank(message = "name不能为空") @RequestParam("name") String name) {
    mcpManageService.updateClient(id, name);
    return ResultEntity.success();
  }

  @ApiOperation(value = "删除令牌")
  @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResultEntity delete(@PathVariable("id") Long id) {
    mcpManageService.deleteClient(id);
    return ResultEntity.success();
  }

  @ApiOperation(value = "令牌列表")
  @PostMapping(value = "/listAll", produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResult<McpClientEntity> listAll(@RequestBody EntitySearchRequest request) {
    return mcpManageService.listClientAll(request);
  }
}
