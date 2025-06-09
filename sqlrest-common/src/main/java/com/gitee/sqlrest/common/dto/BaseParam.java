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

import com.gitee.sqlrest.common.enums.ParamLocationEnum;
import com.gitee.sqlrest.common.enums.ParamTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseParam implements Serializable {

  @ApiModelProperty("ID(前端生成并使用)")
  private String id;

  @ApiModelProperty("参数名")
  private String name;

  @ApiModelProperty("参数类型")
  private ParamTypeEnum type;

  @ApiModelProperty("参数位置")
  private ParamLocationEnum location;

  @ApiModelProperty("是否为数组")
  private Boolean isArray;

  @ApiModelProperty("是否必填")
  private Boolean required;

  @ApiModelProperty("默认值")
  private String defaultValue;

  @ApiModelProperty("参数描述")
  private String remark;
}
