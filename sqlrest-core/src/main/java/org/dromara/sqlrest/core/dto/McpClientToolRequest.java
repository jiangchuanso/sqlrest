// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.dto;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class McpClientToolRequest {

  @NotNull(message = "clientId不能为空")
  @ApiModelProperty("MCP令牌ID")
  private Long clientId;

  @ApiModelProperty("授权可见的Tool ID集合(空集合表示全部禁用)")
  private List<Long> toolIds;
}
