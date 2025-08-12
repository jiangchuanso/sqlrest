// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.exec.module;

import org.dromara.sqlrest.cache.CacheFactory;
import org.dromara.sqlrest.cache.DistributedCache;
import org.dromara.sqlrest.common.consts.Constants;
import org.dromara.sqlrest.core.exec.annotation.Comment;
import org.dromara.sqlrest.core.exec.annotation.Module;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
@Module("cache")
public class CacheVarModule {

  @Resource
  private CacheFactory cacheFactory;

  private DistributedCache getDistributedCache() {
    return cacheFactory.getDistributedCache(Constants.CACHE_NAME_API_VAR);
  }

  @Comment("根据键获取缓存中的值")
  public String get(@Comment("key") String key) {
    DistributedCache cache = getDistributedCache();
    return cache.get(key, String.class);
  }

  @Comment("向缓存中写入指定键的值")
  public void put(@Comment("key") String key, @Comment("value") String value, @Comment("ttl") long ttl) {
    DistributedCache cache = getDistributedCache();
    cache.put(key, value, ttl, TimeUnit.SECONDS);
  }

  @Comment("删除缓存中指定键的值")
  public void evict(@Comment("key") String key) {
    DistributedCache cache = getDistributedCache();
    cache.evict(key);
  }
}
