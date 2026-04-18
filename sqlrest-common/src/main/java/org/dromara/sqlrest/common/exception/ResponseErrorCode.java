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

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.dromara.sqlrest.common.util.I18nUtils;

@Getter
@AllArgsConstructor
public enum ResponseErrorCode {

  SUCCESS(0),
  ERROR_INTERNAL_ERROR(1),
  ERROR_INVALID_ARGUMENT(2),
  ERROR_RESOURCE_NOT_EXISTS(3),
  ERROR_RESOURCE_ALREADY_EXISTS(4),
  ERROR_RESOURCE_ALREADY_USED(5),
  ERROR_RESOURCE_NOT_ONLINE(6),
  ERROR_USER_NOT_EXISTS(7),
  ERROR_USER_PASSWORD_WRONG(8),
  ERROR_INVALID_JDBC_URL(9),
  ERROR_CANNOT_CONNECT_REMOTE(10),
  ERROR_EDIT_ALREADY_PUBLISHED(11),

  ERROR_CLIENT_FORBIDDEN(403),
  ERROR_ACCESS_FORBIDDEN(403),
  ERROR_TOKEN_EXPIRED(401),
  ERROR_PATH_NOT_EXISTS(404),

  ERROR_TOO_MANY_REQUESTS(429),
  ;

  private int code;

  public String getMessage() {
    String prefix = "exception.";
    return I18nUtils.getMessage(prefix + this.name());
  }
}
