// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.persistence.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 连接池配置（存储为 JSON）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoolConfig implements Serializable {

  private static final long serialVersionUID = 1L;

  public static final PoolConfig DEFAULT = PoolConfig.builder()
      .maximumPoolSize(10)
      .minimumIdle(10)
      .maxLifetime(3600000L)
      .connectionTimeout(60000L)
      .idleTimeout(60000)
      .build();

  /**
   * 连接池最大连接数
   */
  private Integer maximumPoolSize;

  /**
   * 连接池最小空闲连接数
   */
  private Integer minimumIdle;

  /**
   * 连接最大存活时间(毫秒)
   */
  private Long maxLifetime;

  /**
   * 连接超时时间(毫秒)
   */
  private Long connectionTimeout;

  /**
   * 空闲连接超时时间(毫秒)
   */
  private Integer idleTimeout;
}
