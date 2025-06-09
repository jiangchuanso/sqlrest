// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.sqlrest.core.exec;

import com.gitee.sqlrest.core.dto.ExecuteSqlRecord;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SqlExecuteLogger {

  private static final ThreadLocal<List<ExecuteSqlRecord>> threadLocal = new ThreadLocal<>();

  public static void init() {
    threadLocal.set(new ArrayList<>());
  }

  public static void add(String sql, List parameters, Long costs) {
    List<ExecuteSqlRecord> list = threadLocal.get();
    if (null != list) {
      list.add(new ExecuteSqlRecord(sql, parameters, costs));
    }
  }

  public static List<ExecuteSqlRecord> get() {
    List<ExecuteSqlRecord> list = threadLocal.get();
    if (null == list) {
      return Collections.emptyList();
    }
    return list;
  }

  public static void clear() {
    threadLocal.remove();
  }
}
