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
import org.dromara.sqlrest.common.enums.ProductTypeEnum;

public abstract class AbstractExecutorEngine implements ApiExecutorEngine {

  protected HikariDataSource dataSource;
  protected ProductTypeEnum productType;
  protected boolean printSqlLog;

  public AbstractExecutorEngine(HikariDataSource dataSource, ProductTypeEnum productType) {
    this.dataSource = dataSource;
    this.productType = productType;
    this.printSqlLog = true;
  }

  @Override
  public void setPrintSqlLog(boolean printSqlLog) {
    this.printSqlLog = printSqlLog;
  }
}
