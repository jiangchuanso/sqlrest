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

import java.util.function.Consumer;
import java.util.function.Function;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Pool;

public class JedisClient {

  private Pool<Jedis> jedisPool;

  public JedisClient(Pool<Jedis> jedisPool) {
    this.jedisPool = jedisPool;
  }

  private Jedis getJedis() {
    Jedis jedis = this.jedisPool.getResource();
    jedis.ping();
    return jedis;
  }

  public <T> T doAction(Function<Jedis, T> function) {
    Jedis jedis = getJedis();
    try {
      return function.apply(jedis);
    } finally {
      jedis.close();
    }
  }

  public void doConsume(Consumer<Jedis> action) {
    Jedis jedis = getJedis();
    try {
      action.accept(jedis);
    } finally {
      jedis.close();
    }
  }
}
