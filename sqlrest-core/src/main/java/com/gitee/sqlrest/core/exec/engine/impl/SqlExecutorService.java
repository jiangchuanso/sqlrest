package com.gitee.sqlrest.core.exec.engine.impl;

import com.gitee.sqlrest.common.enums.NamingStrategyEnum;
import com.gitee.sqlrest.common.enums.ProductTypeEnum;
import com.gitee.sqlrest.core.exec.engine.AbstractExecutorEngine;
import com.gitee.sqlrest.core.util.PageSizeUtils;
import com.gitee.sqlrest.core.util.SqlJdbcUtils;
import com.gitee.sqlrest.persistence.entity.ApiContextEntity;
import com.gitee.sqlrest.template.SqlMeta;
import com.gitee.sqlrest.template.XmlSqlTemplate;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SqlExecutorService extends AbstractExecutorEngine {

  public SqlExecutorService(HikariDataSource dataSource, ProductTypeEnum productType) {
    super(dataSource, productType);
  }

  @Override
  public List<Object> execute(List<ApiContextEntity> scripts, Map<String, Object> params, NamingStrategyEnum strategy) {
    List<Object> dataList = new ArrayList<>();
    try (Connection connection = this.dataSource.getConnection()) {
      try {
        connection.setAutoCommit(false);
        for (ApiContextEntity sql : scripts) {
          XmlSqlTemplate template = new XmlSqlTemplate(sql.getSqlText());
          SqlMeta sqlMeta = template.process(params);
          int page = PageSizeUtils.getPageFromParams(params);
          int size = PageSizeUtils.getSizeFromParams(params);
          Object result = SqlJdbcUtils.execute(productType, connection, sqlMeta, strategy, page, size);
          if (sqlMeta.isQuerySQL()) {
            dataList.add(result);
          }
        }
        connection.commit();
        return dataList;
      } catch (Exception e) {
        try {
          connection.rollback();
        } catch (SQLException se) {
          log.warn("Failed to call jdbc Connection::rollback(): {}", se.getMessage(), se);
        }
        throw e;
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
