package com.gitee.sqlrest.common.dto;

import com.gitee.sqlrest.common.enums.ParamTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("出参信息")
public class OutParam implements Serializable {

  @ApiModelProperty("参数名")
  private String name;

  @ApiModelProperty("参数类型")
  private ParamTypeEnum type;

  @ApiModelProperty("参数描述")
  private String remark;
}
