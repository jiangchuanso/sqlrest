// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.manager.exception;

import java.util.stream.Collectors;
import org.dromara.sqlrest.common.dto.ResultEntity;
import org.dromara.sqlrest.common.exception.CommonException;
import org.dromara.sqlrest.common.exception.ResponseErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice
public class ExceptionController {

  @ExceptionHandler(value = {MethodArgumentNotValidException.class})
  public ResultEntity argumentValidException(MethodArgumentNotValidException e) {
    log.error("Invalid arguments error:", e);

    String errorMessage = e.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining("; "));
    
    return ResultEntity.failed(ResponseErrorCode.ERROR_INVALID_ARGUMENT, errorMessage);
  }

  @ResponseBody
  @ExceptionHandler(value = Exception.class)
  public ResultEntity errorHandler(Exception e) {
    if (e instanceof CommonException) {
      CommonException ex = (CommonException) e;
      return ResultEntity.failed(ex.getCode(), ex.getMessage());
    }

    log.error("Error:", e);
    if (e instanceof NullPointerException) {
      return ResultEntity.failed(ResponseErrorCode.ERROR_INTERNAL_ERROR, "Null Pointer Exception");
    } else {
      return ResultEntity.failed(ResponseErrorCode.ERROR_INTERNAL_ERROR, e.getMessage());
    }

  }
}
