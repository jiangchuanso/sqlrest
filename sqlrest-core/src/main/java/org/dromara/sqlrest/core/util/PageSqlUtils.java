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
import java.util.Objects;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sqlrest.common.enums.ProductTypeEnum;

@Slf4j
@UtilityClass
public class PageSqlUtils {

  public static final String sqlDialect = "show global variables where variable_name = 'ob_compatibility_mode'";

  public static String getPageSql(ProductTypeEnum productType, Connection conn, String sql, Integer page,
      Integer size) {
    if (ProductTypeEnum.OCEANBASE == productType) {
      if (PageSqlUtils.isInMysqlMode(conn)) {
        return ProductTypeEnum.MYSQL.getPageSql(sql, page, size);
      } else {
        return ProductTypeEnum.ORACLE.getPageSql(sql, page, size);
      }
    }
    return productType.getPageSql(sql, page, size);
  }

  public static boolean isInMysqlMode(Connection connection) {
    try (PreparedStatement statement = connection.prepareStatement(sqlDialect)) {
      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          String value = resultSet.getString(2);
          if (Objects.nonNull(value)) {
            if (value.toUpperCase().contains("MYSQL")) {
              return true;
            } else {
              return false;
            }
          } else {
            throw new RuntimeException("Execute SQL[" + sqlDialect + "] return null value");
          }
        } else {
          throw new RuntimeException("Execute SQL[" + sqlDialect + "] no result");
        }
      }
    } catch (SQLException sqlException) {
      if (log.isDebugEnabled()) {
        log.debug("Failed to execute sql :{}, and guesses OceanBase is MySQL Mode!", sqlDialect);
      }
    }
    return true;
  }
}