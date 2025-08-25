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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.dromara.sqlrest.common.service.DisplayRecord;
import org.dromara.sqlrest.core.dto.ExecuteSqlRecord;
import org.dromara.sqlrest.core.dto.ScripDebugRecord;

public final class DebugExecuteLogger {

  private static final ThreadLocal<List<DisplayRecord>> threadLocal = new ThreadLocal<>();

  public static void init() {
    threadLocal.set(new ArrayList<>());
  }

  public static void add(String sql, List parameters, Long costs) {
    List<DisplayRecord> list = threadLocal.get();
    if (null != list) {
      list.add(new ExecuteSqlRecord(sql, parameters, costs));
    }
  }

  public static void add(String text) {
    List<DisplayRecord> list = threadLocal.get();
    if (null != list) {
      list.add(new ScripDebugRecord(text));
    }
  }

  public static List<DisplayRecord> get() {
    List<DisplayRecord> list = threadLocal.get();
    if (null == list) {
      return Collections.emptyList();
    }
    return list;
  }

  public static void clear() {
    threadLocal.remove();
  }
}
