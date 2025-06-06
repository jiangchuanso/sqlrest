package com.gitee.sqlrest.core.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel("统一告警配置")
public class TestAlarmConfigRequest {

  @NotBlank(message = "endpoint不能为空")
  @ApiModelProperty("接口端点")
  private String endpoint;

  @NotBlank(message = "contentType不能为空")
  @ApiModelProperty("入参格式类型")
  private String contentType;

  @NotBlank(message = "inputTemplate不能为空")
  @ApiModelProperty("入参模板")
  private String inputTemplate;

  @NotEmpty(message = "dataModel不能为空")
  @ApiModelProperty("示例数据")
  private List<NameValueBaseResponse> dataModel;
}
