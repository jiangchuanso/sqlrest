// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.sqlrest.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("参数信息")
public class ParamValue extends BaseParam {

  @ApiModelProperty("Object类型的子元素及值")
  private List<BaseParamValue> children;

  @ApiModelProperty("参数值")
  private String value;

  @Data
  public static class BaseParamValue extends BaseParam {

    private String value;
  }
}
