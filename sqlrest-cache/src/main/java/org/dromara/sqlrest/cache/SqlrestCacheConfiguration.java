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

import com.hazelcast.config.Config;
import com.hazelcast.eureka.one.EurekaOneDiscoveryStrategyFactory;
import com.netflix.discovery.EurekaClient;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.sqlrest.cache.hazelcast.HazelcastCacheFactory;
import org.dromara.sqlrest.cache.redis.JedisClient;
import org.dromara.sqlrest.cache.redis.RedisCacheFactory;
import org.dromara.sqlrest.cache.redis.RedisProperties;
import org.dromara.sqlrest.common.consts.Constants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.util.Pool;

@Configuration
public class SqlrestCacheConfiguration {

  @Configuration
  @ConditionalOnProperty(value = "sqlrest.cache.hazelcast.enabled", havingValue = "true")
  static class HazelcastCacheConfig {

    @Bean
    public Config hazelcastCacheConfigFromEureka(EurekaClient eurekaClient) {
      EurekaOneDiscoveryStrategyFactory.setEurekaClient(eurekaClient);
      Config config = new Config();
      config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
      config.getNetworkConfig().getJoin().getEurekaConfig()
          .setEnabled(true)
          .setProperty("self-registration", "true")
          .setProperty("namespace", "hazelcast")
          .setProperty("use-metadata-for-host-and-port", "true")
          .setProperty("skip-eureka-registration-verification", "true");
      config.getMapConfig(Constants.CACHE_KEY_TOKEN_CLIENT)
          .setTimeToLiveSeconds(Constants.CLIENT_TOKEN_DURATION_SECONDS.intValue());
      return config;
    }

    @Bean
    public CacheFactory hazelcastCacheFactory() {
      return new HazelcastCacheFactory();
    }
  }

  @Configuration
  @EnableConfigurationProperties(RedisProperties.class)
  @ConditionalOnProperty(value = "sqlrest.cache.redis.enabled", havingValue = "true")
  static class RedisCacheConfig {

    @Bean
    public Pool<Jedis> jedisPool(RedisProperties properties) {
      RedisProperties.Pool pool = properties.getPool();
      JedisPoolConfig poolConfig = new JedisPoolConfig();
      poolConfig.setMaxTotal(pool.getMaxActive());
      poolConfig.setMaxIdle(pool.getMaxIdle());
      poolConfig.setMinIdle(pool.getMinIdle());
      if (pool.getTimeBetweenEvictionRuns() != null) {
        poolConfig.setTimeBetweenEvictionRunsMillis(pool.getTimeBetweenEvictionRuns().toMillis());
      }
      if (pool.getMaxWait() != null) {
        poolConfig.setMaxWaitMillis(pool.getMaxWait().toMillis());
      }

      int timeout = (int) properties.getTimeout().toMillis();
      Set<String> sentinels = Optional.ofNullable(properties.getSentinel())
          .map(
              s -> Optional.ofNullable(s.getNodes())
                  .orElseGet(ArrayList::new)
                  .stream().collect(Collectors.toSet())
          ).orElseGet(HashSet::new);
      if (CollectionUtils.isNotEmpty(sentinels)) {
        return new JedisSentinelPool(
            properties.getSentinel().getMaster(),
            sentinels,
            poolConfig,
            timeout,
            properties.getUsername(),
            properties.getPassword(),
            properties.getDatabase(),
            properties.getClientName());
      } else {
        return new JedisPool(
            poolConfig,
            properties.getHost(),
            properties.getPort(),
            timeout,
            properties.getUsername(),
            properties.getPassword(),
            properties.getDatabase(),
            properties.getClientName(),
            properties.isSsl());
      }
    }

    @Bean
    public JedisClient jedisClient(Pool<Jedis> jedisPool) {
      return new JedisClient(jedisPool);
    }

    @Bean
    public CacheFactory redisCacheFactory() {
      return new RedisCacheFactory();
    }
  }

}
