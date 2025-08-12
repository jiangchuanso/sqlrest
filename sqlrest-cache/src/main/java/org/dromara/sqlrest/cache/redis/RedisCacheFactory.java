// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.cache.redis;

import org.dromara.sqlrest.cache.CacheFactory;
import org.dromara.sqlrest.cache.DistributedCache;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Resource;

public class RedisCacheFactory implements CacheFactory {

  private Map<String, RedisDistributedCache> cacheMap = new ConcurrentHashMap<>();

  @Resource
  private JedisClient jedisClient;

  @Override
  public <T> Map<String, T> getCacheMap(String key, Class<T> clazz) {
    return new RedisCacheMap<>(key, jedisClient, clazz);
  }

  @Override
  public DistributedCache getDistributedCache(String name) {
    return cacheMap.computeIfAbsent(name, key -> new RedisDistributedCache(key, jedisClient));
  }
}
