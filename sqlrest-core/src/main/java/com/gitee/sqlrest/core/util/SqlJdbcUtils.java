package com.gitee.sqlrest.core.util;

import com.gitee.sqlrest.template.SqlMeta;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class SqlJdbcUtils {

  public static Object execute(Connection connection, SqlMeta sqlMeta, int page, int size) throws SQLException {
    List<Object> paramValues = sqlMeta.getParameter();
    PreparedStatement statement = connection.prepareStatement(sqlMeta.getSql());
    statement.setQueryTimeout(300);
    statement.setFetchSize(isMySqlConnection(connection) ? Integer.MIN_VALUE : 100);
    for (int i = 1; i <= paramValues.size(); i++) {
      statement.setObject(i, paramValues.get(i - 1));
    }
    log.info("ExecuteSQL:{}\n{}", sqlMeta.getSql(), paramValues);
    if (statement.execute()) {
      int skipNumber = size * (page - 1);
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
          if (skipNumber <= 0) {
            list.add(row);
            if (list.size() >= size) {
              break;
            }
          } else {
            skipNumber--;
          }
        }
        return list;
      }
    } else {
      int updateCount = statement.getUpdateCount();
      return "(" + updateCount + ") rows affected";
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
