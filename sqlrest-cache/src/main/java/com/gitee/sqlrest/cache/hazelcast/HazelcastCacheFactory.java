package com.gitee.sqlrest.cache.hazelcast;

import com.gitee.sqlrest.cache.CacheFactory;
import com.gitee.sqlrest.cache.DistributedCache;
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
