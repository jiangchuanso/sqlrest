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

@Getter
@AllArgsConstructor
public enum ResponseErrorCode {

  SUCCESS(0, "success"),
  ERROR_INTERNAL_ERROR(1, "internal error"),
  ERROR_INVALID_ARGUMENT(2, "invalid arguments"),
  ERROR_RESOURCE_NOT_EXISTS(3, "resource not exists"),
  ERROR_RESOURCE_ALREADY_EXISTS(4, "resource already exists"),
  ERROR_RESOURCE_ALREADY_USED(5, "resource already been used"),
  ERROR_RESOURCE_NOT_ONLINE(6, "resource not online"),
  ERROR_USER_NOT_EXISTS(7, "user not exists"),
  ERROR_USER_PASSWORD_WRONG(8, "invalid password"),
  ERROR_INVALID_JDBC_URL(9, "invalid jdbc url format"),
  ERROR_CANNOT_CONNECT_REMOTE(10, "remote address not reach"),
  ERROR_EDIT_ALREADY_PUBLISHED(11, "can not edit already publish(online)"),

  ERROR_CLIENT_FORBIDDEN(403, "client is forbidden"),
  ERROR_ACCESS_FORBIDDEN(403, "access forbidden"),
  ERROR_TOKEN_EXPIRED(401, "token is expired"),
  ERROR_PATH_NOT_EXISTS(404, "path not exists"),

  ERROR_TOO_MANY_REQUESTS(429, "too many requests"),
  ;

  private int code;
  private String message;
}
