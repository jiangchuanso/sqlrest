// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.exec.engine.impl;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sqlrest.common.enums.NamingStrategyEnum;
import org.dromara.sqlrest.common.enums.ProductTypeEnum;
import org.dromara.sqlrest.common.util.LambdaUtils;
import org.dromara.sqlrest.core.exec.engine.AbstractExecutorEngine;
import org.dromara.sqlrest.core.util.PageSizeUtils;
import org.dromara.sqlrest.core.util.SqlJdbcUtils;
import org.dromara.sqlrest.persistence.entity.ApiContextEntity;
import org.dromara.sqlrest.template.SqlMeta;
import org.dromara.sqlrest.template.XmlSqlTemplate;

@Slf4j
public class SqlExecutorService extends AbstractExecutorEngine {

  public SqlExecutorService(HikariDataSource dataSource, ProductTypeEnum productType) {
    super(dataSource, productType);
  }

  @Override
  public List<Object> execute(List<ApiContextEntity> scripts, Map<String, Object> params, NamingStrategyEnum strategy) {
    List<Object> dataList = new ArrayList<>();
    try (Connection connection = this.dataSource.getConnection()) {
      boolean supportsTx = connection.getMetaData().supportsTransactions();
      try {
        LambdaUtils.ifDo(supportsTx, () -> connection.setAutoCommit(false));
        for (ApiContextEntity sql : scripts) {
          XmlSqlTemplate template = new XmlSqlTemplate(sql.getSqlText());
          SqlMeta sqlMeta = template.process(params);
          int page = PageSizeUtils.getPageFromParams(params);
          int size = PageSizeUtils.getSizeFromParams(params);
          Object result = SqlJdbcUtils.execute(productType, connection, sqlMeta, strategy, page, size, printSqlLog);
          if (sqlMeta.isQuerySQL()) {
            dataList.add(result);
          }
        }
        LambdaUtils.ifDo(supportsTx, () -> connection.commit());
        return dataList;
      } catch (Exception e) {
        LambdaUtils.ifDoIgnoreThrow(supportsTx, () -> connection.rollback());
        throw e;
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
