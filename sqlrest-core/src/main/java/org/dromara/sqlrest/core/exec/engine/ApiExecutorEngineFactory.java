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

import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import org.dromara.sqlrest.common.enums.ExecuteEngineEnum;
import org.dromara.sqlrest.common.enums.ProductTypeEnum;
import org.dromara.sqlrest.core.exec.engine.impl.ScriptExecutorService;
import org.dromara.sqlrest.core.exec.engine.impl.SqlExecutorService;

public class ApiExecutorEngineFactory {

  private static Map<ExecuteEngineEnum, BiFunction<HikariDataSource, ProductTypeEnum, ApiExecutorEngine>> engineMap = new HashMap<>();

  static {
    engineMap.put(ExecuteEngineEnum.SQL, SqlExecutorService::new);
    engineMap.put(ExecuteEngineEnum.SCRIPT, ScriptExecutorService::new);
  }

  public static ApiExecutorEngine getExecutor(ExecuteEngineEnum engine, HikariDataSource dataSource,
      ProductTypeEnum productType, boolean printSqlLog) {
    BiFunction<HikariDataSource, ProductTypeEnum, ApiExecutorEngine> creator = engineMap.get(engine);
    if (null == creator) {
      throw new RuntimeException("Unsupported engine :" + engine);
    }
    ApiExecutorEngine executorEngine = creator.apply(dataSource, productType);
    executorEngine.setPrintSqlLog(printSqlLog);
    return executorEngine;
  }

}
