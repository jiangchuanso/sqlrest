// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.executor.config;

import javax.annotation.Resource;
import javax.servlet.http.HttpServlet;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sqlrest.common.consts.Constants;
import org.dromara.sqlrest.core.exec.ApiExecuteService;
import org.dromara.sqlrest.core.servlet.AuthenticationFilter;
import org.dromara.sqlrest.executor.model.HttpApiServlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableScheduling
@Configuration
public class ExecutorServletConfig {

  private static final String URL_PATH_PATTERN = String.format("/%s/*", Constants.API_PATH_PREFIX);

  @Value("${sqlrest.executor.print-sql-log}")
  private boolean printSqlLog;

  @Resource
  private AuthenticationFilter authenticationFilter;

  /**
   * API接口认证
   *
   * @return FilterRegistrationBean
   */
  @Bean
  public FilterRegistrationBean authFilterRegistrationBean() {
    FilterRegistrationBean registrationBean = new FilterRegistrationBean();
    registrationBean.setFilter(authenticationFilter);
    registrationBean.addUrlPatterns(URL_PATH_PATTERN);
    registrationBean.setOrder(2);
    log.info("Register authFilter for {} UrlPatterns, and order is {}", URL_PATH_PATTERN, 2);
    return registrationBean;
  }

  /**
   * API接口处理
   *
   * @return ServletRegistrationBean
   */
  @Bean
  public ServletRegistrationBean apiServletRegistrationBean(ApiExecuteService apiExecuteService) {
    HttpServlet httpServlet = new HttpApiServlet(apiExecuteService, printSqlLog);
    return new ServletRegistrationBean(httpServlet, URL_PATH_PATTERN);
  }
}
