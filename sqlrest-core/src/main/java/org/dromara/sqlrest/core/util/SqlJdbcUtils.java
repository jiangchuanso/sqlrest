// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sqlrest.common.enums.NamingStrategyEnum;
import org.dromara.sqlrest.common.enums.ProductTypeEnum;
import org.dromara.sqlrest.common.util.LambdaUtils;
import org.dromara.sqlrest.core.exec.logger.DebugExecuteLogger;
import org.dromara.sqlrest.template.SqlMeta;

@Slf4j
@UtilityClass
public class SqlJdbcUtils {

  private static final int QUERY_TIMEOUT = 300;

  public static Function<String, String> getConverter(NamingStrategyEnum strategy) {
    return Objects.isNull(strategy) ? Function.identity() : strategy.getFunction();
  }

  public static Object execute(ProductTypeEnum productType, Connection connection, SqlMeta sqlMeta,
      NamingStrategyEnum strategy, int page, int size, boolean printSqlLog) throws SQLException {
    List<Object> paramValues = sqlMeta.getParameter();
    boolean isQuerySql = sqlMeta.isQuerySQL();
    String sql = isQuerySql
        ? PageSqlUtils.getPageSql(productType, connection, sqlMeta.getSql(), page, size)
        : sqlMeta.getSql();
    Consumer<Connection> executeBeforeQuery = productType.getContext().getExecuteBeforeQuery();
    LambdaUtils.ifDo(Objects.nonNull(executeBeforeQuery), () -> executeBeforeQuery.accept(connection));
    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setQueryTimeout(QUERY_TIMEOUT);
    statement.setFetchSize(isMySqlConnection(connection) ? Integer.MIN_VALUE : size);
    LambdaUtils.ifDo(isQuerySql, () -> productType.getPageConsumer().accept(page, size, paramValues));
    for (int i = 1; i <= paramValues.size(); i++) {
      statement.setObject(i, paramValues.get(i - 1));
    }

    if (printSqlLog) {
      log.info("ExecuteSQL:{}\n{}", sql, paramValues);
    }

    long start = System.currentTimeMillis();
    try {
      Function<String, String> converter = getConverter(strategy);
      if (statement.execute()) {
        try (ResultSet rs = statement.getResultSet()) {
          List<String> columns = new ArrayList<>();
          for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
            String columnName = rs.getMetaData().getColumnLabel(i);
            columns.add(columnName);
          }
          List<Map<String, Object>> list = new ArrayList<>();
          while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (String column : columns) {
              try {
                row.put(column, rs.getObject(column));
              } catch (SQLException se) {
                log.warn("Failed to call jdbc ResultSet::getObject(): {}", se.getMessage(), se);
                row.put(column, null);
              }
            }
            list.add(ConvertUtils.to(row, converter));
          }
          return list;
        }
      } else {
        int updateCount = statement.getUpdateCount();
        return "(" + updateCount + ") rows affected";
      }
    } finally {
      DebugExecuteLogger.add(sql, paramValues, System.currentTimeMillis() - start);
    }
  }

  private boolean isMySqlConnection(Connection connection) {
    try {
      String productName = connection.getMetaData().getDatabaseProductName();
      return productName.contains("MySQL") || productName.contains("MariaDB");
    } catch (Exception e) {
      return false;
    }
  }
}
