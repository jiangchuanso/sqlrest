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

import java.util.Map;
import org.dromara.sqlrest.core.exec.annotation.Comment;
import org.dromara.sqlrest.core.exec.annotation.Module;

@Module("req")
public class ReqVarModule {

  private Map<String, Object> params;

  public ReqVarModule(Map<String, Object> params) {
    this.params = params;
  }

  @Comment("设置一个请求参数，如果存在同名将被覆盖")
  public void setParam(@Comment("name") String name, @Comment("value") Object value) {
    this.params.put(name, value);
  }
}
