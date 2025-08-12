// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.common.exception;

import lombok.Data;

@Data
public class CommonException extends RuntimeException {

  private ResponseErrorCode code;

  public CommonException(ResponseErrorCode code, String message) {
    super(message);
    this.code = code;
  }

  public CommonException(ResponseErrorCode code, Throwable cause) {
    super(cause);
    this.code = code;
  }
}
