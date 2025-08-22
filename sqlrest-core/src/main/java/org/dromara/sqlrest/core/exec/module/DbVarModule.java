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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sqlrest.common.enums.NamingStrategyEnum;
import org.dromara.sqlrest.common.enums.ProductTypeEnum;
import org.dromara.sqlrest.core.exec.logger.SqlExecuteLogger;
import org.dromara.sqlrest.core.exec.annotation.Comment;
import org.dromara.sqlrest.core.exec.annotation.Module;
import org.dromara.sqlrest.core.util.ConvertUtils;
import org.dromara.sqlrest.core.util.PageSizeUtils;
import org.dromara.sqlrest.core.util.PageSqlUtils;
import org.dromara.sqlrest.template.SqlMeta;
import org.dromara.sqlrest.template.XmlSqlTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;

@Slf4j
@Module("db")
public class DbVarModule {

  private DataSource dataSource;
  private JdbcTemplate jdbcTemplate;
  private ProductTypeEnum productType;
  private Map<String, Object> params;
  private Function<String, String> converter;

  public DbVarModule(DataSource dataSource, ProductTypeEnum productType, Map<String, Object> params,
      NamingStrategyEnum strategy) {
    this.dataSource = dataSource;
    this.jdbcTemplate = new JdbcTemplate(dataSource);
    this.productType = productType;
    this.params = params;

    if (null == strategy) {
      strategy = NamingStrategyEnum.NONE;
    }
    this.converter = strategy.getFunction();
  }

  private String getPageSql(String sql, int page, int size) {
    if (!productType.isMultiDialect()) {
      return productType.getPageSql(sql, page, size);
    }
    try (Connection connection = dataSource.getConnection()) {
      return PageSqlUtils.getPageSql(productType, connection, sql, page, size);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private Map<String, Object> build(Map<String, Object> row) {
    return ConvertUtils.to(row, converter);
  }

  private List<Map<String, Object>> build(List<Map<String, Object>> rows) {
    return rows.stream().map(this::build).collect(Collectors.toList());
  }

  @Comment("查询所有的数据列表")
  public List<Map<String, Object>> selectAll(@Comment("sqlOrXml") String sqlOrXml) throws SQLException {
    log.info("Enter selectAll() function, SQL:{},params:{}", sqlOrXml, params);
    XmlSqlTemplate template = new XmlSqlTemplate(sqlOrXml);
    SqlMeta sqlMeta = template.process(params);
    long start = System.currentTimeMillis();
    try {
      return build(jdbcTemplate.queryForList(sqlMeta.getSql(), sqlMeta.getParameter().toArray()));
    } finally {
      SqlExecuteLogger.add(sqlMeta.getSql(), sqlMeta.getParameter(), System.currentTimeMillis() - start);
    }
  }

  @Comment("count所有数据的总数")
  public Integer selectCount(@Comment("sqlOrXml") String sqlOrXml) {
    log.info("Enter selectCount() function, SQL:{},params:{}", sqlOrXml, params);
    XmlSqlTemplate template = new XmlSqlTemplate(sqlOrXml);
    SqlMeta sqlMeta = template.process(params);
    String countSql = String.format("select count(*) from (%s) a", sqlMeta.getSql());
    long start = System.currentTimeMillis();
    try {
      return jdbcTemplate.queryForObject(countSql, Integer.class, sqlMeta.getParameter().toArray());
    } finally {
      SqlExecuteLogger.add(countSql, sqlMeta.getParameter(), System.currentTimeMillis() - start);
    }
  }

  @Comment("查询单条结果，并传入变量信息，查不到返回null")
  public Map<String, Object> selectOne(@Comment("sqlOrXml") String sqlOrXml) {
    log.info("Enter selectOne() function, SQL:{},params:{}", sqlOrXml, params);
    XmlSqlTemplate template = new XmlSqlTemplate(sqlOrXml);
    SqlMeta sqlMeta = template.process(params);
    long start = System.currentTimeMillis();
    try {
      return build(jdbcTemplate
          .query(sqlMeta.getSql(), new ResultSetExtractor<Map<String, Object>>() {
                private ColumnMapRowMapper mapper = new ColumnMapRowMapper();

                @Override
                public Map<String, Object> extractData(ResultSet rs) throws SQLException, DataAccessException {
                  if (rs.next()) {
                    return mapper.mapRow(rs, 0);
                  }
                  return null;
                }
              },
              sqlMeta.getParameter().toArray()));
    } finally {
      SqlExecuteLogger.add(sqlMeta.getSql(), sqlMeta.getParameter(), System.currentTimeMillis() - start);
    }
  }

  @Comment("分页查询数据列表")
  public List<Map<String, Object>> page(@Comment("sqlOrXml") String sqlOrXml)
      throws SQLException {
    log.info("Enter page() function, SQL:{},params:{}", sqlOrXml, params);
    int page = PageSizeUtils.getPageFromParams(params);
    int size = PageSizeUtils.getSizeFromParams(params);
    XmlSqlTemplate template = new XmlSqlTemplate(sqlOrXml);
    SqlMeta sqlMeta = template.process(params);
    String pageSql = getPageSql(sqlMeta.getSql(), page, size);
    List<Object> parameters = sqlMeta.getParameter();
    this.productType.getPageConsumer().accept(page, size, parameters);
    long start = System.currentTimeMillis();
    try {
      return build(jdbcTemplate.queryForList(pageSql, parameters.toArray()));
    } finally {
      SqlExecuteLogger.add(pageSql, parameters, System.currentTimeMillis() - start);
    }
  }

  @Comment("执行insert操作，返回插入主键")
  public Map<String, Object> insert(@Comment("sqlOrXml") String sqlOrXml) {
    log.info("Enter insert() function, SQL:{},params:{}", sqlOrXml, params);
    XmlSqlTemplate template = new XmlSqlTemplate(sqlOrXml);
    SqlMeta sqlMeta = template.process(params);
    List<Object> parameters = sqlMeta.getParameter();
    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    long start = System.currentTimeMillis();
    try {
      jdbcTemplate.update(
          connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlMeta.getSql(), Statement.RETURN_GENERATED_KEYS);
            new ArgumentPreparedStatementSetter(parameters.toArray()).setValues(ps);
            return ps;
          },
          keyHolder);
      return build(keyHolder.getKeys());
    } finally {
      SqlExecuteLogger.add(sqlMeta.getSql(), parameters, System.currentTimeMillis() - start);
    }
  }

  @Comment("执行update操作，返回受影响行数")
  public int update(@Comment("sqlOrXml") String sqlOrXml) {
    log.info("Enter update() function, SQL:{},params:{}", sqlOrXml, params);
    XmlSqlTemplate template = new XmlSqlTemplate(sqlOrXml);
    SqlMeta sqlMeta = template.process(params);
    List<Object> parameters = sqlMeta.getParameter();
    long start = System.currentTimeMillis();
    try {
      return jdbcTemplate.update(sqlMeta.getSql(), parameters.toArray());
    } finally {
      SqlExecuteLogger.add(sqlMeta.getSql(), parameters, System.currentTimeMillis() - start);
    }
  }

  @Comment("批量执行操作，返回受影响的行数")
  public int batchUpdate(@Comment("sqlList") List<String> sqlList) {
    log.info("Enter batchUpdate() function, SQL:{},params:{}", sqlList);
    long start = System.currentTimeMillis();
    try {
      return Arrays.stream(jdbcTemplate.batchUpdate(sqlList.toArray(new String[0]))).sum();
    } finally {
      SqlExecuteLogger.add(sqlList.stream().collect(Collectors.joining(";")),
          Collections.emptyList(), System.currentTimeMillis() - start);
    }
  }

  @Comment("执行delete操作，返回受影响行数")
  public int delete(@Comment("sqlOrXml") String sqlOrXml) {
    log.info("Enter update() function, SQL:{},params:{}", sqlOrXml, params);
    XmlSqlTemplate template = new XmlSqlTemplate(sqlOrXml);
    SqlMeta sqlMeta = template.process(params);
    List<Object> parameters = sqlMeta.getParameter();
    long start = System.currentTimeMillis();
    try {
      return jdbcTemplate.update(sqlMeta.getSql(), parameters.toArray());
    } finally {
      SqlExecuteLogger.add(sqlMeta.getSql(), parameters, System.currentTimeMillis() - start);
    }
  }
}

