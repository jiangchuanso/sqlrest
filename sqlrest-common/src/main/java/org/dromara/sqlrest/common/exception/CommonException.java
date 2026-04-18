/////////////////////////////////////////////////////////////
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
import org.dromara.sqlrest.common.util.I18nUtils;

@Data
public class CommonException extends RuntimeException {

  private ResponseErrorCode code;

  public CommonException(ResponseErrorCode code, String message) {
    super(I18nUtils.getMessage(message));
    this.code = code;
  }

  public CommonException(ResponseErrorCode code, String messageKey, Object... args) {
    super(I18nUtils.getMessage(messageKey, args));
    this.code = code;
  }

  public CommonException(ResponseErrorCode code, Throwable cause) {
    super(cause);
    this.code = code;
  }
}
