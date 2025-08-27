// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class ConvertUtils {

  public static Map<String, Object> to(Map<String, Object> row) {
    return to(row, null);
  }

  public static Map<String, Object> to(Map<String, Object> row, Function<String, String> converter) {
    if (null == converter) {
      return row;
    }
    if (null == row) {
      return null;
    }
    Map<String, Object> ret = new LinkedHashMap<>();
    row.forEach((key, val) -> ret.put(converter.apply(key), val));
    return ret;
  }

}
