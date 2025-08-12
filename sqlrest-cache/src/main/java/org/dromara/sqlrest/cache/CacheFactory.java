// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.cache;

import java.util.Map;

public interface CacheFactory {

  <T> Map<String, T> getCacheMap(String key, Class<T> clazz);

  DistributedCache getDistributedCache(String name);
}
