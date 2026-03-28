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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dromara.sqlrest.common.enums.NodeStatusEnum;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("拓扑节点信息")
public class TopologyNodeResponse {

  @ApiModelProperty("服务ID")
  private String serviceId;

  @ApiModelProperty("实例ID")
  private String instanceId;

  @ApiModelProperty("主机地址")
  private String host;

  @ApiModelProperty("端口号")
  private Integer port;


  @ApiModelProperty("内存使用")
  private Integer memory;

  @ApiModelProperty("CPU使用")
  private Integer cpu;

  @ApiModelProperty("存储使用")
  private Integer disk;

  @ApiModelProperty("节点状态")
  private NodeStatusEnum status;
}
