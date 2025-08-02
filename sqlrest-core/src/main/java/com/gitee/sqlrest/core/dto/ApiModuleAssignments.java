package com.gitee.sqlrest.core.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("模块接口树")
public class ApiModuleAssignments extends EntityIdNameResponse {

  @ApiModelProperty("接口列表")
  private List<SelectedEntityIdName> children;

}
