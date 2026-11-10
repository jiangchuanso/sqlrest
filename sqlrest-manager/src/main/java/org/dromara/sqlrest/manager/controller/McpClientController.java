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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.dromara.sqlrest.common.consts.Constants;
import org.dromara.sqlrest.common.dto.IdWithName;
import org.dromara.sqlrest.common.dto.PageResult;
import org.dromara.sqlrest.common.dto.ResultEntity;
import org.dromara.sqlrest.core.dto.EntitySearchRequest;
import org.dromara.sqlrest.core.dto.McpClientToolRequest;
import org.dromara.sqlrest.core.dto.McpServerAddrResponse;
import org.dromara.sqlrest.manager.service.McpManageService;
import org.dromara.sqlrest.persistence.entity.McpClientEntity;
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
@RequiredArgsConstructor
public class McpClientController {

  private final McpManageService mcpManageService;

  @ApiOperation(value = "获取MCP服务地址")
  @GetMapping(value = "/endpoint", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResultEntity<McpServerAddrResponse> getMcpServerEndpoint() {
    return ResultEntity.success(mcpManageService.getMcpServerEndpoint());
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

  @ApiOperation(value = "保存令牌授权")
  @PostMapping(value = "/auth/create", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResultEntity createToolAuth(@Valid @RequestBody McpClientToolRequest request) {
    mcpManageService.saveClientToolAuth(request);
    return ResultEntity.success();
  }

  @ApiOperation(value = "回显令牌授权")
  @GetMapping(value = "/auth/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResultEntity<List<IdWithName>> getToolAuth(@PathVariable("id") Long id) {
    return ResultEntity.success(mcpManageService.getClientToolAuth(id));
  }
}
