package com.gitee.sqlrest.core.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel("数据源保存")
public class DataSourceSaveRequest extends DataSourceBaseRequest {

  @ApiModelProperty("ID编号")
  private Long id;
}
