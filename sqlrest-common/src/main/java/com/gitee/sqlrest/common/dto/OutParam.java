package com.gitee.sqlrest.common.dto;

import com.gitee.sqlrest.common.enums.ParamTypeEnum;
import com.gitee.sqlrest.common.exception.CommonException;
import com.gitee.sqlrest.common.exception.ResponseErrorCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("出参信息")
public class OutParam implements Serializable {

  @ApiModelProperty("ID(前端生成并使用)")
  private String id;

  @ApiModelProperty("参数名")
  private String name;

  @ApiModelProperty("参数类型")
  private ParamTypeEnum type;

  @ApiModelProperty("是否为数组")
  private Boolean isArray;

  @ApiModelProperty("参数描述")
  private String remark;

  @ApiModelProperty("Object类型的子元素")
  private List<OutParam> children;

  public void checkValid() {
    if (StringUtils.isBlank(getName())) {
      throw new CommonException(ResponseErrorCode.ERROR_INTERNAL_ERROR, "output parameter name must is not blank");
    }
    if (null == getType()) {
      throw new CommonException(ResponseErrorCode.ERROR_INTERNAL_ERROR, "output parameter type must is not empty");
    }

    if (getType() == ParamTypeEnum.OBJECT) {
      if (null != children && children.size() > 0) {
        for (OutParam param : children) {
          if (StringUtils.isBlank(param.getName())) {
            throw new CommonException(ResponseErrorCode.ERROR_INTERNAL_ERROR,
                "output parameter name must is not blank");
          }
        }
      } else {
        throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT,
            "Object output param '" + getName() + "' must have child parameter.");
      }
    } else {
      children = Collections.emptyList();
    }
  }
}
