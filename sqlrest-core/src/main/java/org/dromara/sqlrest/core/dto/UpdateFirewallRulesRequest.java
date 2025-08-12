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

import org.dromara.sqlrest.common.enums.OnOffEnum;
import org.dromara.sqlrest.common.enums.WhiteBlackEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel("防火墙规则")
public class UpdateFirewallRulesRequest {

  @NotNull(message = "status不能为null")
  @ApiModelProperty("开启状态")
  private OnOffEnum status;

  @ApiModelProperty("黑白名单选项")
  private WhiteBlackEnum mode;

  @ApiModelProperty("地址列表")
  private String addresses;
}
