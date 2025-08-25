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

import java.text.MessageFormat;
import lombok.NoArgsConstructor;
import org.dromara.sqlrest.common.service.VarModuleInterface;
import org.dromara.sqlrest.core.exec.annotation.Comment;
import org.dromara.sqlrest.core.exec.annotation.Module;
import org.dromara.sqlrest.core.exec.logger.DebugExecuteLogger;
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

  @Comment("打印调试日志信息")
  public void print(@Comment("message") String message, Object... arguments) {
    DebugExecuteLogger.add(MessageFormat.format(message, arguments));
  }
}
