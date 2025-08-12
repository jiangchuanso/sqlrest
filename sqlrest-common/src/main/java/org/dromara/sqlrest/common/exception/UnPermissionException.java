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

public class UnPermissionException extends RuntimeException {

  public UnPermissionException(String message) {
    super(message);
  }

  public UnPermissionException(String message, Throwable cause) {
    super(message, cause);
  }
}
