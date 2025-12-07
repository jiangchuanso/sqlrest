// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.gateway;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableDiscoveryClient
@EnableScheduling
@MapperScan("org.dromara.sqlrest.persistence.mapper")
@SpringBootApplication(
    scanBasePackages = {
        "org.dromara.sqlrest.persistence",
        "org.dromara.sqlrest.core.gateway",
        "org.dromara.sqlrest.gateway",
    }
)
public class GatewayApplication {

  public static void main(String[] args) {
    SpringApplication springApplication = new SpringApplication(GatewayApplication.class);
    springApplication.setBannerMode(Banner.Mode.OFF);
    springApplication.run(args);
  }
}
