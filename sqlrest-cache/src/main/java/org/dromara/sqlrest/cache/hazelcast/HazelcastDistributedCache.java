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

import org.dromara.sqlrest.cache.DistributedCache;
import com.hazelcast.core.HazelcastInstance;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class HazelcastDistributedCache implements DistributedCache {

  private final HazelcastInstance instance;
  private final String name;

  public HazelcastDistributedCache(HazelcastInstance instance, String name) {
    this.instance = Objects.requireNonNull(instance, "instance must not be null");
    this.name = Objects.requireNonNull(name, "cache name must not be null");
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public <T> T get(String key, Class<T> type) {
    Object value = instance.getMap(name).get(key);
    return Optional.ofNullable(value).map(v -> type.cast(v)).orElse(null);
  }

  @Override
  public void put(String key, Object value, long expire, TimeUnit unit) {
    if (expire > 0) {
      instance.getMap(name).put(key, value, expire, unit);
    } else {
      instance.getMap(name).put(key, value);
    }
  }

  @Override
  public void evict(String key) {
    instance.getMap(name).evict(key);
  }
}
