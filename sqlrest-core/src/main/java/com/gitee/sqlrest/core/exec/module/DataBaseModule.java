package com.gitee.sqlrest.core.exec.module;

import cn.hutool.core.util.NumberUtil;
import com.gitee.sqlrest.common.consts.Constants;
import com.gitee.sqlrest.common.enums.ProductTypeEnum;
import com.gitee.sqlrest.script.annotation.Comment;
import com.gitee.sqlrest.script.runtime.RuntimeContext;
import com.gitee.sqlrest.template.Configuration;
import com.gitee.sqlrest.template.SqlMeta;
import com.gitee.sqlrest.template.SqlTemplate;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;

@Slf4j
public class DataBaseModule {

  private static Pattern REPLACE_ORDER_BY = Pattern
      .compile("order\\s+by\\s+[^,\\s]+(\\s+asc|\\s+desc)?(\\s*,\\s*[^,\\s]+(\\s+asc|\\s+desc)?)*",
          Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
  private static Configuration cfg = new Configuration();

  private JdbcTemplate jdbcTemplate;
  private ProductTypeEnum productType;

  public DataBaseModule(DataSource dataSource, ProductTypeEnum productType) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
    this.productType = productType;
  }

  @Comment("查询所有的数据列表")
  public List<Map<String, Object>> selectAll(RuntimeContext context, String sqlOrXml) throws SQLException {
    Map<String, Object> params = context.getVarMap();
    log.info("Enter selectAll() function, SQL:{},params:{}", sqlOrXml, params);
    SqlTemplate template = cfg.getTemplate(sqlOrXml);
    SqlMeta sqlMeta = template.process(params);
    return jdbcTemplate.queryForList(sqlMeta.getSql(), sqlMeta.getParameter().toArray());
  }

  @Comment("count所有数据的总数")
  public Integer selectCount(RuntimeContext context, String sqlOrXml) {
    Map<String, Object> params = context.getVarMap();
    log.info("Enter selectCount() function, SQL:{},params:{}", sqlOrXml, params);
    SqlTemplate template = cfg.getTemplate(sqlOrXml);
    SqlMeta sqlMeta = template.process(params);
    String sql = REPLACE_ORDER_BY.matcher(sqlMeta.getSql()).replaceAll("");
    String countSql = String.format("select count(*) from (%s) a", sql);
    return jdbcTemplate.queryForObject(countSql, Integer.class, sqlMeta.getParameter().toArray());
  }

  @Comment("查询单条结果，并传入变量信息，查不到返回null")
  public Map<String, Object> selectOne(RuntimeContext context, String sqlOrXml) {
    Map<String, Object> params = context.getVarMap();
    SqlTemplate template = cfg.getTemplate(sqlOrXml);
    SqlMeta sqlMeta = template.process(params);
    return jdbcTemplate
        .query(sqlMeta.getSql(), new ResultSetExtractor<Map<String, Object>>() {
              private ColumnMapRowMapper mapper = new ColumnMapRowMapper();

              @Override
              public Map<String, Object> extractData(ResultSet rs) throws SQLException, DataAccessException {
                return mapper.mapRow(rs, 0);
              }
            },
            sqlMeta.getParameter().toArray());
  }

  @Comment("分页查询数据列表")
  public List<Map<String, Object>> page(RuntimeContext context, String sqlOrXml)
      throws SQLException {
    Map<String, Object> params = context.getVarMap();
    log.info("Enter page() function, SQL:{},params:{}", sqlOrXml, params);
    SqlTemplate template = cfg.getTemplate(sqlOrXml);
    SqlMeta sqlMeta = template.process(params);
    String pageSql = productType.getPageSql(sqlMeta.getSql());
    List<Object> parameters = sqlMeta.getParameter();
    int page = NumberUtil.parseInt(params.getOrDefault(Constants.PARAM_PAGE_NUMBER, 1).toString());
    int size = NumberUtil.parseInt(params.getOrDefault(Constants.PARAM_PAGE_SIZE, 10).toString());
    parameters.add(((page - 1) * size) < 0 ? 0 : (page - 1) * size);
    parameters.add(size);
    return jdbcTemplate.queryForList(pageSql, parameters.toArray());
  }

  @Comment("执行insert操作，返回插入主键")
  public Map<String, Object> insert(RuntimeContext context, String sqlOrXml) {
    Map<String, Object> params = context.getVarMap();
    log.info("Enter insert() function, SQL:{},params:{}", sqlOrXml, params);
    SqlTemplate template = cfg.getTemplate(sqlOrXml);
    SqlMeta sqlMeta = template.process(params);
    List<Object> parameters = sqlMeta.getParameter();
    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(
        connection -> {
          PreparedStatement ps = connection.prepareStatement(sqlMeta.getSql(), Statement.RETURN_GENERATED_KEYS);
          new ArgumentPreparedStatementSetter(parameters.toArray()).setValues(ps);
          return ps;
        },
        keyHolder);
    return keyHolder.getKeys();
  }

  @Comment("执行update操作，返回受影响行数")
  public int update(RuntimeContext context, String sqlOrXml) {
    Map<String, Object> params = context.getVarMap();
    log.info("Enter update() function, SQL:{},params:{}", sqlOrXml, params);
    SqlTemplate template = cfg.getTemplate(sqlOrXml);
    SqlMeta sqlMeta = template.process(params);
    List<Object> parameters = sqlMeta.getParameter();
    return jdbcTemplate.update(sqlMeta.getSql(), parameters.toArray());
  }

  @Comment("批量执行操作，返回受影响的行数")
  public int batchUpdate(List<String> sqlList) {
    log.info("Enter batchUpdate() function, SQL:{},params:{}", sqlList);
    return Arrays.stream(jdbcTemplate.batchUpdate(sqlList.toArray(new String[0]))).sum();
  }

  @Comment("执行delete操作，返回受影响行数")
  public int delete(RuntimeContext context, String sqlOrXml) {
    Map<String, Object> params = context.getVarMap();
    log.info("Enter update() function, SQL:{},params:{}", sqlOrXml, params);
    SqlTemplate template = cfg.getTemplate(sqlOrXml);
    SqlMeta sqlMeta = template.process(params);
    List<Object> parameters = sqlMeta.getParameter();
    return jdbcTemplate.update(sqlMeta.getSql(), parameters.toArray());
  }
}

