package com.gitee.sqlrest.core.dto;

import com.gitee.sqlrest.common.enums.OnOffEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel("统一告警配置")
public class UpdateAlarmConfigRequest {

  @NotNull(message = "status不能为null")
  @ApiModelProperty("开启状态")
  private OnOffEnum status;

  @NotBlank(message = "endpoint不能为空")
  @ApiModelProperty("接口端点")
  private String endpoint;

  @NotBlank(message = "contentType不能为空")
  @ApiModelProperty("入参格式类型")
  private String contentType;

  @NotBlank(message = "inputTemplate不能为空")
  @ApiModelProperty("入参模板")
  private String inputTemplate;
}
