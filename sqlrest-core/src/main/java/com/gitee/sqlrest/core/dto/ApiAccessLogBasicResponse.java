package com.gitee.sqlrest.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("API调用日志记录")
public class ApiAccessLogBasicResponse {

  @ApiModelProperty("ID编号")
  private Long id;

  @ApiModelProperty("HTTP状态码")
  private Integer status;

  @ApiModelProperty("耗时")
  private Long duration;

  @ApiModelProperty("客户端地址")
  private String ipAddr;

  @ApiModelProperty("客户端UA")
  private String userAgent;

  @ApiModelProperty("应用名称")
  private String clientApp;

  @ApiModelProperty("错误异常")
  private String exception;

  @ApiModelProperty("记录时间")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private Timestamp createTime;
}
