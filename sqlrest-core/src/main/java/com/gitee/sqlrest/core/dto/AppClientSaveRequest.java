package com.gitee.sqlrest.core.dto;

import com.gitee.sqlrest.common.enums.ExpireTimeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel("客户端应用信息")
public class AppClientSaveRequest {

  @NotBlank(message = "name不能为空")
  @ApiModelProperty("客户端应用名称")
  private String name;

  @ApiModelProperty("客户端应用描述")
  private String description;

  @NotBlank(message = "appKey不能为空")
  @ApiModelProperty("应用AppKey账号")
  private String appKey;

  @NotNull(message = "expireTime不能为null")
  @ApiModelProperty("到期时间")
  private ExpireTimeEnum expireTime;
}
