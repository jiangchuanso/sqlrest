package com.gitee.sqlrest.common.exception;

public class UnPermissionException extends RuntimeException {

  public UnPermissionException(String message) {
    super(message);
  }

  public UnPermissionException(String message, Throwable cause) {
    super(message, cause);
  }
}
