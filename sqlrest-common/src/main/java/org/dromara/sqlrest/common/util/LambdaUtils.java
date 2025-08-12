// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.common.util;

import org.dromara.sqlrest.common.model.ThrowableRunnable;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LambdaUtils {

  public static void ifDo(boolean condition, ThrowableRunnable action) {
    if (condition) {
      try {
        action.run();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static void ifDoMayThrow(boolean condition, ThrowableRunnable action) throws Exception {
    action.run();
  }

  public static void ifDoIgnoreThrow(boolean condition, ThrowableRunnable action) {
    try {
      action.run();
    } catch (Exception ignore) {  // NOSORNA
    }
  }

  public static void ifDoElse(boolean condition, ThrowableRunnable trueAction, ThrowableRunnable falseAction) {
    try {
      if (condition) {
        trueAction.run();
      } else {
        falseAction.run();
      }
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

}
