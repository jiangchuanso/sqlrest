package org.dromara.sqlrest.core.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ApiModel("客户端应用列表搜索")
public class AppClientSearchRequest extends EntitySearchRequest {

  @ApiModelProperty("分组ID")
  private Long groupId;
}
