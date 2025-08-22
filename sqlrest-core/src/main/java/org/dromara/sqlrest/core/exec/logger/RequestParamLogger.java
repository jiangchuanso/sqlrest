// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.exec.logger;

import java.util.Map;

public class RequestParamLogger {

  private static final ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<>();

  public static void set(Map<String, Object> requestParams) {
    threadLocal.set(requestParams);
  }

  public static Map<String, Object> getAndClear() {
    Map<String, Object> r = threadLocal.get();
    threadLocal.remove();
    return r;
  }

}
