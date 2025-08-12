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

import org.dromara.sqlrest.core.exec.annotation.Comment;
import org.dromara.sqlrest.core.exec.annotation.Module;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@Module("env")
public class EnvVarModule {

  private final Environment environment;

  public EnvVarModule(Environment environment) {
    this.environment = environment;
  }

  @Comment("获取配置")
  public String get(@Comment("key") String key) {
    return environment.getProperty(key);
  }

  @Comment("获取配置")
  public String get(@Comment("key") String key,
      @Comment("defaultValue") String defaultValue) {
    return environment.getProperty(key, defaultValue);
  }

}
