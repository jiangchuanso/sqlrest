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

import lombok.NoArgsConstructor;
import org.dromara.sqlrest.common.service.VarModuleInterface;
import org.dromara.sqlrest.core.exec.annotation.Comment;
import org.dromara.sqlrest.core.exec.annotation.Module;
import org.dromara.sqlrest.core.exec.logger.DebugExecuteLogger;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
@Module(LogVarModule.VAR_NAME)
public class LogVarModule implements VarModuleInterface {

  protected static final String VAR_NAME = "log";

  @Override
  public String getVarModuleName() {
    return VAR_NAME;
  }

  @Comment("comment.log.print")
  public void print(@Comment("comment.param.message") String message) {
    DebugExecuteLogger.add(message);
  }

  @Comment("comment.log.print")
  public void print(@Comment("comment.param.message") String message, @Comment("comment.param.arguments") Object... arguments) {
    // https://blog.csdn.net/weixin_44792849/article/details/131854226
    DebugExecuteLogger.add(MessageFormatter.arrayFormat(message, arguments).getMessage());
  }
}
