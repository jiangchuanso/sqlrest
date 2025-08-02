package com.gitee.sqlrest.core.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectedEntityIdName {

  @ApiModelProperty("ID编号")
  private Long id;

  @ApiModelProperty("名称")
  private String name;

  @ApiModelProperty("是否选中")
  private Boolean selected;
}
