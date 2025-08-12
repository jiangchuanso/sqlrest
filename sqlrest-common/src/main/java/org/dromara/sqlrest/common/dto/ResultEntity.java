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

import org.dromara.sqlrest.common.exception.ResponseErrorCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;

@ApiModel(description = "响应结果")
@AllArgsConstructor
@Data
public class ResultEntity<T> implements Serializable {

  private static final String SUCCESS = "success";

  @ApiModelProperty("状态码")
  private Integer code;

  @ApiModelProperty("状态描述")
  private String message;

  @ApiModelProperty("数据")
  private T data;

  public static <T> ResultEntity success() {
    return new ResultEntity(0, SUCCESS, null);
  }

  public static <T> ResultEntity success(T data) {
    return new ResultEntity<>(0, SUCCESS, data);
  }

  public static ResultEntity failed(ResponseErrorCode code) {
    return new ResultEntity(code.getCode(), code.getMessage(), null);
  }

  public static ResultEntity failed(ResponseErrorCode code, String message) {
    return new ResultEntity(code.getCode(), code.getMessage() + "," + message, null);
  }

  public static ResultEntity failed(String message) {
    return new ResultEntity(ResponseErrorCode.ERROR_INTERNAL_ERROR.getCode(), message, null);
  }
}
