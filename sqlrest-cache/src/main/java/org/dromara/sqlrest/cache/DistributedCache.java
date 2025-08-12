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

import java.util.concurrent.TimeUnit;

/**
 * 支持分布式的缓存接口定义
 */
public interface DistributedCache {

  /**
   * 获取cache名称
   *
   * @return String
   */
  String getName();

  /**
   * 指定key获取值
   *
   * @param key  键
   * @param type 类型
   * @param <T>  泛型
   * @return T
   */
  <T> T get(String key, Class<T> type);

  /**
   * 向cache中写入缓存值
   *
   * @param key    键
   * @param value  值
   * @param expire 过期时长
   * @param unit   过期时间单位
   */
  void put(String key, Object value, long expire, TimeUnit unit);

  /**
   * 删除指定key的缓存数据
   *
   * @param key 键
   */
  void evict(String key);
}
