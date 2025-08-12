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

import org.dromara.sqlrest.common.consts.Constants;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class ApiPathUtils {

  public static String getFullPath(String path) {
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    return String.format("/%s/%s", Constants.API_PATH_PREFIX, path);
  }
}
