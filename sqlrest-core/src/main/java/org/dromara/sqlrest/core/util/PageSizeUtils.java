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

import cn.hutool.core.util.NumberUtil;
import org.dromara.sqlrest.common.consts.Constants;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PageSizeUtils {

  public static int getPageFromParams(Map<String, Object> params) {
    int page = (null == params.get(Constants.PARAM_PAGE_NUMBER))
        ? 1
        : NumberUtil.parseInt(params.get(Constants.PARAM_PAGE_NUMBER).toString());
    if (page <= 0) {
      page = 1;
    }
    return page;
  }

  public static int getSizeFromParams(Map<String, Object> params) {
    int size = (null == params.get(Constants.PARAM_PAGE_SIZE))
        ? 10
        : NumberUtil.parseInt(params.get(Constants.PARAM_PAGE_SIZE).toString());
    if (size <= 0) {
      size = 10;
    }
    return size;
  }
  
}
