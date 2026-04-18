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

import org.dromara.sqlrest.common.service.VarModuleInterface;
import org.dromara.sqlrest.core.exec.annotation.Comment;
import org.dromara.sqlrest.core.exec.annotation.Module;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@Module(EnvVarModule.VAR_NAME)
public class EnvVarModule implements VarModuleInterface {

  protected static final String VAR_NAME = "env";

  private final Environment environment;

  public EnvVarModule(Environment environment) {
    this.environment = environment;
  }

  @Override
  public String getVarModuleName() {
    return VAR_NAME;
  }

  @Comment("comment.env.get")
  public String get(@Comment("comment.param.key") String key) {
    return environment.getProperty(key);
  }

  @Comment("comment.env.get")
  public String get(@Comment("comment.param.key") String key,
      @Comment("comment.param.defaultValue") String defaultValue) {
    return environment.getProperty(key, defaultValue);
  }

}
