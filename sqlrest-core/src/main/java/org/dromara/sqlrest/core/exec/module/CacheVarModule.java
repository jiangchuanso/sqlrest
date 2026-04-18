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

import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.dromara.sqlrest.cache.CacheFactory;
import org.dromara.sqlrest.cache.DistributedCache;
import org.dromara.sqlrest.common.consts.Constants;
import org.dromara.sqlrest.common.service.VarModuleInterface;
import org.dromara.sqlrest.core.exec.annotation.Comment;
import org.dromara.sqlrest.core.exec.annotation.Module;
import org.springframework.stereotype.Service;

@Service
@Module(CacheVarModule.VAR_NAME)
public class CacheVarModule implements VarModuleInterface {

  protected static final String VAR_NAME = "cache";

  @Resource
  private CacheFactory cacheFactory;

  @Override
  public String getVarModuleName() {
    return VAR_NAME;
  }

  private DistributedCache getDistributedCache() {
    return cacheFactory.getDistributedCache(Constants.CACHE_NAME_API_VAR);
  }

  @Comment("comment.cache.get")
  public String get(@Comment("comment.param.key") String key) {
    DistributedCache cache = getDistributedCache();
    return cache.get(key, String.class);
  }

  @Comment("comment.cache.put")
  public void put(@Comment("comment.param.key") String key, @Comment("comment.param.paramValue") String value, @Comment("comment.param.ttl") long ttl) {
    DistributedCache cache = getDistributedCache();
    cache.put(key, value, ttl, TimeUnit.SECONDS);
  }

  @Comment("comment.cache.evict")
  public void evict(@Comment("comment.param.key") String key) {
    DistributedCache cache = getDistributedCache();
    cache.evict(key);
  }
}
