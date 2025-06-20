// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.sqlrest.core.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel("MCP工具配置")
public class McpToolSaveRequest {

  @ApiModelProperty("ID编号(保存接口使用)")
  private Long id;

  @NotNull(message = "apiId不能为null")
  @ApiModelProperty("API的ID")
  private Long apiId;

  @NotBlank(message = "name不能为空")
  @ApiModelProperty("工具名称")
  private String name;

  @NotBlank(message = "description不能为空")
  @ApiModelProperty("工具描述")
  private String description;
}
