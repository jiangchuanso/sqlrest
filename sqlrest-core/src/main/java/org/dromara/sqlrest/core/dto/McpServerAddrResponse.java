package org.dromara.sqlrest.core.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("MCP服务端地址前缀")
public class McpServerAddrResponse {

  @ApiModelProperty("SSE地址的路径")
  private String sseAddrPrefix;

  @ApiModelProperty("StreamHttp地址的路径")
  private String streamAddrPrefix;
}
