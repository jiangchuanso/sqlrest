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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.LinkedList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("SQL参数解析")
public class SqlParamParseResponse {

  @ApiModelProperty("参数名")
  private String name;

  @ApiModelProperty("是否为数组")
  private Boolean isArray;

  @ApiModelProperty("Object类型的子元素")
  private List<SqlParamParseResponse> children;

  public SqlParamParseResponse(String name, Boolean isArray) {
    this.name = name;
    this.isArray = isArray;
    this.children = new LinkedList<>();
  }
}
