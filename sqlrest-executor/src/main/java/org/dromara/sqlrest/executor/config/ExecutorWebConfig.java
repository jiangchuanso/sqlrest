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

import org.dromara.sqlrest.common.consts.Constants;
import org.dromara.sqlrest.executor.interceptor.ExecutorInterceptor;
import org.dromara.sqlrest.persistence.dao.SystemParamDao;
import javax.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ExecutorWebConfig implements WebMvcConfigurer {

  private final String pathPattern = Constants.API_DOC_PATH_PREFIX + "/**";

  @Resource
  private SystemParamDao systemParamDao;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler(pathPattern).addResourceLocations("classpath:/apidoc/");
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    ExecutorInterceptor interceptor = new ExecutorInterceptor(systemParamDao);
    registry.addInterceptor(interceptor).addPathPatterns(pathPattern);
  }

}
