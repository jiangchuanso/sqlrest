package com.gitee.sqlrest.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("TOKEN信息")
public class AccessToken implements Serializable {

  @ApiModelProperty("实际名称")
  private String realName;

  @ApiModelProperty("唯一标识")
  private String appKey;

  @ApiModelProperty("token字符串")
  private String accessToken;

  @ApiModelProperty("有效时间(单位秒)")
  private Long expireSeconds;
}
