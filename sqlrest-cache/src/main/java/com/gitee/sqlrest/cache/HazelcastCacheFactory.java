package com.gitee.sqlrest.cache;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class HazelcastCacheFactory {

  @Resource
  private HazelcastInstance hazelcastInstance;

  public <T> IMap<String, T> getCacheMap(String key) {
    return hazelcastInstance.getMap(key);
  }

}
