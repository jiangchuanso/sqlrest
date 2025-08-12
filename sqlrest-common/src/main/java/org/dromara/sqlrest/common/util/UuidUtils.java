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

import java.util.UUID;
import lombok.experimental.UtilityClass;

/**
 * UUID工具类
 */
@UtilityClass
public final class UuidUtils {

  public static String generateUuid() {
    return UUID.randomUUID().toString().replace("-", "");
  }

}
