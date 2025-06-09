// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.sqlrest.gateway;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@MapperScan("com.gitee.sqlrest.persistence.mapper")
@SpringBootApplication(
    scanBasePackages = {
        "com.gitee.sqlrest.persistence",
        "com.gitee.sqlrest.core.gateway",
        "com.gitee.sqlrest.gateway",
    }
)
public class GatewayApplication {

  public static void main(String[] args) {
    SpringApplication springApplication = new SpringApplication(GatewayApplication.class);
    springApplication.setBannerMode(Banner.Mode.OFF);
    springApplication.run(args);
  }
}
