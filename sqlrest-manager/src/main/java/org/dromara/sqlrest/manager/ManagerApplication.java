// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.manager;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableEurekaServer
@EnableDiscoveryClient
@EnableScheduling
@MapperScan("org.dromara.sqlrest.persistence.mapper")
@SpringBootApplication(
    scanBasePackages = {
        "org.dromara.sqlrest.persistence",
        "org.dromara.sqlrest.core.driver",
        "org.dromara.sqlrest.core.gateway",
        "org.dromara.sqlrest.core.executor",
        "org.dromara.sqlrest.core.service",
        "org.dromara.sqlrest.core.exec",
        "org.dromara.sqlrest.cache",
        "org.dromara.sqlrest.manager"
    }
)
public class ManagerApplication {

  public static void main(String[] args) {
    SpringApplication springApplication = new SpringApplication(ManagerApplication.class);
    springApplication.setBannerMode(Banner.Mode.OFF);
    springApplication.run(args);
  }
}
