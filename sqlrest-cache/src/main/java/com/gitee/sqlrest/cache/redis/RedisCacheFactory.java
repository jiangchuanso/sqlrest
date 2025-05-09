package com.gitee.sqlrest.cache.redis;

import com.gitee.sqlrest.cache.CacheFactory;
import com.gitee.sqlrest.cache.DistributedCache;
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
