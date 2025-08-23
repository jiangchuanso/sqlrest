// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.exec.engine;

import org.dromara.sqlrest.common.enums.NamingStrategyEnum;
import org.dromara.sqlrest.persistence.entity.ApiContextEntity;
import java.util.List;
import java.util.Map;

public interface ApiExecutorEngine {

  void setPrintSqlLog(boolean printSqlLog);

  List<Object> execute(List<ApiContextEntity> scripts, Map<String, Object> params, NamingStrategyEnum strategy);
}
