// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.cache.hazelcast;

import org.dromara.sqlrest.cache.CacheFactory;
import org.dromara.sqlrest.cache.DistributedCache;
import com.hazelcast.core.HazelcastInstance;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Resource;

public class HazelcastCacheFactory implements CacheFactory {

  private Map<String, HazelcastDistributedCache> cacheMap = new ConcurrentHashMap<>();

  @Resource
  private HazelcastInstance hazelcastInstance;

  @Override
  public <T> Map<String, T> getCacheMap(String key, Class<T> clazz) {
    return hazelcastInstance.getMap(key);
  }

  @Override
  public DistributedCache getDistributedCache(String name) {
    return cacheMap.computeIfAbsent(name, key -> new HazelcastDistributedCache(hazelcastInstance, key));
  }
}
