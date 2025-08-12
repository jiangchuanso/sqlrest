// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.common.dto;

import org.dromara.sqlrest.common.enums.HttpMethodEnum;
import org.dromara.sqlrest.common.enums.ParamTypeEnum;
import org.dromara.sqlrest.common.exception.CommonException;
import org.dromara.sqlrest.common.exception.ResponseErrorCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("入参信息")
public class ItemParam extends BaseParam {

  @ApiModelProperty("Object类型的子元素")
  private List<BaseParam> children;

  public void checkValid(HttpMethodEnum method) {
    if (StringUtils.isBlank(getName())) {
      throw new CommonException(ResponseErrorCode.ERROR_INTERNAL_ERROR, "input parameter name must is not blank");
    }
    if (getType() == ParamTypeEnum.OBJECT) {
      if (!method.isHasBody()) {
        throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT,
            "Request with GET/HEAD method cannot have object input parameter as body.");
      }
      if (null != children && children.size() > 0) {
        for (BaseParam param : children) {
          if (StringUtils.isBlank(param.getName())) {
            throw new CommonException(ResponseErrorCode.ERROR_INTERNAL_ERROR, "input parameter name must is not blank");
          }
        }
      } else {
        throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT,
            "Object input param '" + getName() + "' must have child parameter.");
      }
    } else {
      children = Collections.emptyList();
    }
  }
}
