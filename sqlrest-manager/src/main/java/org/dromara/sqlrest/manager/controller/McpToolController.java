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

import java.util.List;
import org.dromara.sqlrest.common.consts.Constants;
import org.dromara.sqlrest.common.dto.IdWithName;
import org.dromara.sqlrest.common.dto.PageResult;
import org.dromara.sqlrest.common.dto.ResultEntity;
import org.dromara.sqlrest.core.dto.EntitySearchRequest;
import org.dromara.sqlrest.core.dto.McpToolResponse;
import org.dromara.sqlrest.core.dto.McpToolSaveRequest;
import org.dromara.sqlrest.manager.service.McpManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import javax.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"MCP工具管理接口"})
@RestController
@RequestMapping(value = Constants.MANGER_API_V1 + "/mcp/tool")
@RequiredArgsConstructor
public class McpToolController {

  private final McpManageService mcpManageService;

  @ApiOperation(value = "添加MCP工具")
  @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResultEntity create(@Valid @RequestBody McpToolSaveRequest request) {
    mcpManageService.createTool(request);
    return ResultEntity.success();
  }

  @ApiOperation(value = "更新MCP工具")
  @PostMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResultEntity update(@Valid @RequestBody McpToolSaveRequest request) {
    mcpManageService.updateTool(request);
    return ResultEntity.success();
  }

  @ApiOperation(value = "删除MCP工具")
  @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResultEntity delete(@PathVariable("id") Long id) {
    mcpManageService.deleteTool(id);
    return ResultEntity.success();
  }

  @ApiOperation(value = "MCP工具列表")
  @PostMapping(value = "/listAll", produces = MediaType.APPLICATION_JSON_VALUE)
  public PageResult<McpToolResponse> listAll(@RequestBody EntitySearchRequest request) {
    return mcpManageService.listToolAll(request);
  }

  @ApiOperation(value = "全部Tool选项")
  @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResultEntity<List<IdWithName>> list() {
    return ResultEntity.success(mcpManageService.listToolOptions());
  }
}
