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

import java.time.Duration;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "sqlrest.cache.redis")
public class RedisProperties {

  @Data
  public static class Pool {

    private int maxIdle = 8;
    private int minIdle = 0;
    private int maxActive = 8;
    private Duration maxWait = Duration.ofMillis(-1L);
    private Duration timeBetweenEvictionRuns;
  }

  @Data
  public static class Sentinel {

    private String master;
    private List<String> nodes;
  }

  private boolean enabled = false;
  private int database = 0;
  private String host = "127.0.0.1";
  private int port = 6379;
  private String username;
  private String password;
  private boolean ssl = false;
  private Duration timeout = Duration.ofSeconds(1);
  private Duration connectTimeout = Duration.ofSeconds(1);
  private String clientName = "sqlrest";
  private Pool pool = new Pool();
  private Sentinel sentinel = new Sentinel();
}
