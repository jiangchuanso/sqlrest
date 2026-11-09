// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.dromara.sqlrest.common.dto.PageResult;
import org.dromara.sqlrest.common.enums.ProductTypeEnum;
import org.dromara.sqlrest.common.exception.CommonException;
import org.dromara.sqlrest.common.exception.ResponseErrorCode;
import org.dromara.sqlrest.common.util.JdbcUrlUtils;
import org.dromara.sqlrest.core.driver.DriverLoadService;
import org.dromara.sqlrest.core.dto.DataSourceBaseRequest;
import org.dromara.sqlrest.core.dto.DataSourceSaveRequest;
import org.dromara.sqlrest.core.dto.DatabaseTypeDetailResponse;
import org.dromara.sqlrest.core.dto.DatasourceDetailResponse;
import org.dromara.sqlrest.core.dto.EntityIdNameResponse;
import org.dromara.sqlrest.core.dto.EntitySearchRequest;
import org.dromara.sqlrest.core.dto.MetadataColumnResponse;
import org.dromara.sqlrest.core.dto.MetadataTableResponse;
import org.dromara.sqlrest.core.util.DataSourceUtils;
import org.dromara.sqlrest.core.util.MetadataCommentSql;
import org.dromara.sqlrest.persistence.dao.ApiAssignmentDao;
import org.dromara.sqlrest.persistence.dao.ApiOnlineDao;
import org.dromara.sqlrest.persistence.dao.DataSourceDao;
import org.dromara.sqlrest.persistence.entity.DataSourceEntity;
import org.dromara.sqlrest.persistence.util.PageUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSourceService {

  private final DataSourceDao dataSourceDao;

  public List<DatabaseTypeDetailResponse> getTypes() {
    List<DatabaseTypeDetailResponse> lists = new ArrayList<>();
    for (ProductTypeEnum type : ProductTypeEnum.values()) {
      DatabaseTypeDetailResponse detail = new DatabaseTypeDetailResponse();
      detail.setId(type.getId());
      detail.setType(type.getName().toUpperCase());
      detail.setDriver(type.getDriver());
      detail.setSample(type.getSample());
      lists.add(detail);
    }

    return lists;
  }

  public PageResult<DatasourceDetailResponse> searchList(EntitySearchRequest request) {
    Supplier<List<DatasourceDetailResponse>> method = () -> {
      List<DataSourceEntity> list = dataSourceDao.listAll(request.getSearchText());
      return list.stream().map(dataSourceEntity -> {
        DatasourceDetailResponse response = new DatasourceDetailResponse();
        DataSourceUtils.decrypt(dataSourceEntity);
        BeanUtil.copyProperties(dataSourceEntity, response);
        return response;
      }).collect(Collectors.toList());
    };

    return PageUtils.getPage(method, request.getPage(), request.getSize());
  }

  public DatasourceDetailResponse getDetailById(Long id) {
    DataSourceEntity dataSourceEntity = dataSourceDao.getById(id);
    DatasourceDetailResponse response = new DatasourceDetailResponse();
    DataSourceUtils.decrypt(dataSourceEntity);
    BeanUtil.copyProperties(dataSourceEntity, response);
    return response;
  }

  private void testConnection(HikariDataSource ds, ProductTypeEnum type) {
    try (Connection connection = ds.getConnection()) {
      if (StringUtils.isNotBlank(type.getSql())) {
        try (Statement statement = connection.createStatement()) {
          statement.execute(type.getSql());
        }
      } else {
        if (!connection.isValid(2)) {
          throw new RuntimeException("Connection is invalid!");
        }
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void preTest(DataSourceBaseRequest request) {
    DataSourceEntity dataSourceEntity = DataSourceEntity.builder()
        .name(request.getName())
        .type(request.getType())
        .driver(request.getDriver())
        .version(request.getVersion())
        .url(request.getUrl())
        .username(request.getUsername())
        .password(request.getPassword())
        .poolConfig(request.getPoolConfig())
        .build();
    File driverPathFile = SpringUtil.getBean(DriverLoadService.class)
        .getVersionDriverFile(dataSourceEntity.getType(),
            dataSourceEntity.getVersion());
    String driverPath = driverPathFile.getAbsolutePath();
    HikariDataSource ds = DataSourceUtils.createDataSource(dataSourceEntity, driverPath);
    try {
      testConnection(ds, request.getType());
    } finally {
      DataSourceUtils.closeHikariDataSource(ds);
    }
  }

  public void testDataSource(Long id) {
    DataSourceEntity dataSourceEntity = dataSourceDao.getById(id);
    if (null == dataSourceEntity) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_NOT_EXISTS, "common.id.not.found", id);
    }
    File driverPathFile = SpringUtil.getBean(DriverLoadService.class)
        .getVersionDriverFile(dataSourceEntity.getType(),
            dataSourceEntity.getVersion());
    String driverPath = driverPathFile.getAbsolutePath();
    HikariDataSource ds = DataSourceUtils.getHikariDataSource(dataSourceEntity, driverPath);
    testConnection(ds, dataSourceEntity.getType());
  }

  public void createDataSource(DataSourceSaveRequest request) {
    if (Objects.nonNull(dataSourceDao.getByName(request.getName()))) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_ALREADY_EXISTS,
          "datasource.name.exists", request.getName());
    }

    DataSourceEntity dataSourceEntity = new DataSourceEntity();
    BeanUtil.copyProperties(request, dataSourceEntity);

    validJdbcUrlFormat(dataSourceEntity);
    DataSourceUtils.encrypt(dataSourceEntity);
    dataSourceDao.insert(dataSourceEntity);
  }

  public void updateDataSource(DataSourceSaveRequest request) {
    if (Objects.isNull(request.getId()) || Objects.isNull(dataSourceDao.getById(request.getId()))) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_NOT_EXISTS, "common.id.not.found", request.getId());
    }

    DataSourceEntity exist = dataSourceDao.getByName(request.getName());
    if (Objects.nonNull(exist) && !exist.getId().equals(request.getId())) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_ALREADY_EXISTS, "common.name.exists", request.getName());
    }

    DataSourceEntity dataSourceEntity = new DataSourceEntity();
    BeanUtil.copyProperties(request, dataSourceEntity);

    validJdbcUrlFormat(dataSourceEntity);
    DataSourceUtils.encrypt(dataSourceEntity);
    dataSourceDao.updateById(dataSourceEntity);
    DataSourceUtils.dropHikariDataSource(request.getId());
  }

  public void deleteDataSource(Long id) {
    if (SpringUtil.getBean(ApiAssignmentDao.class).existsDataSourceById(id)) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_ALREADY_USED, "common.id.not.found", id);
    }
    if (SpringUtil.getBean(ApiOnlineDao.class).existsDataSourceById(id)) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_ALREADY_USED, "common.id.not.found", id);
    }
    dataSourceDao.deleteById(id);
    DataSourceUtils.dropHikariDataSource(id);
  }

  public PageResult<EntityIdNameResponse> getDataSourceNames(Integer page, Integer size) {
    Supplier<List<EntityIdNameResponse>> method = () -> {
      List<DataSourceEntity> lists = dataSourceDao.listAll(null);
      return lists.stream()
          .map(c -> new EntityIdNameResponse(c.getId(), c.getName()))
          .collect(Collectors.toList());
    };

    return PageUtils.getPage(method, page, size);
  }

  public List<String> getDatasourceSchemas(Long id) {
    DataSourceEntity dataSourceEntity = dataSourceDao.getById(id);
    String sqlList = dataSourceEntity.getType().getContext().getSqlSchemaList();
    if (null == sqlList) {
      List<String> ret = dataSourceEntity.getType().getContext().getRetSchemaList();
      if (null != ret) {
        return ret;
      }
    }

    File driverPathFile = SpringUtil.getBean(DriverLoadService.class)
        .getVersionDriverFile(dataSourceEntity.getType(),
            dataSourceEntity.getVersion());
    String driverPath = driverPathFile.getAbsolutePath();
    HikariDataSource ds = DataSourceUtils.getHikariDataSource(dataSourceEntity, driverPath);

    List<String> result = new ArrayList<>();
    try (Connection connection = ds.getConnection()) {
      if (StringUtils.isNotBlank(sqlList)) {
        try (Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sqlList)) {
          while (rs.next()) {
            result.add(rs.getString(1));
          }
        } catch (Exception e) {
          log.warn("Query schema list by [{}] failed:{}", sqlList, e.getMessage());
        }
      }
      List<String> names = distinct(result);
      if (!names.isEmpty()) {
        return names;
      }

      names = distinct(queryNames(connection,
          MetadataCommentSql.schemaNames(dataSourceEntity.getType())));
      if (!names.isEmpty()) {
        log.warn("Fallback schema list from data dictionary for {} : {} schemas",
            dataSourceEntity.getType(), names.size());
        return names;
      }

      try {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getSchemas(connection.getCatalog(), null)) {
          while (rs.next()) {
            result.add(rs.getString("TABLE_SCHEM"));
          }
        }
        if (CollectionUtils.isEmpty(result)) {
          try (ResultSet catalogRs = metaData.getCatalogs()) {
            while (catalogRs.next()) {
              result.add(catalogRs.getString("TABLE_CAT"));
            }
          }
        }
      } catch (Exception e) {
        log.warn("Read schemas by JDBC metadata failed:{}", e.getMessage());
      }
      names = distinct(result);
      if (!names.isEmpty()) {
        return names;
      }
      try {
        String user = connection.getMetaData().getUserName();
        if (StringUtils.isNotBlank(user)) {
          return Collections.singletonList(user);
        }
      } catch (Exception e) {
        log.warn("Read current database user failed:{}", e.getMessage());
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return Collections.emptyList();
  }

  public List<MetadataTableResponse> getSchemaTables(Long id, String schema) {
    DataSourceEntity dataSourceEntity = dataSourceDao.getById(id);
    File driverPathFile = SpringUtil.getBean(DriverLoadService.class)
        .getVersionDriverFile(dataSourceEntity.getType(),
            dataSourceEntity.getVersion());
    String driverPath = driverPathFile.getAbsolutePath();
    ProductTypeEnum productType = dataSourceEntity.getType();
    List<MetadataTableResponse> result = new ArrayList<>();
    HikariDataSource ds = DataSourceUtils.getHikariDataSource(dataSourceEntity, driverPath);
    try (Connection connection = ds.getConnection()) {
      try {
        String catalogName = productType.getContext().isHasCatalogAndSchema()
            ? connection.getCatalog()
            : getAdapter(productType, connection, schema).getLeft();
        String schemaName = getAdapter(productType, connection, schema).getRight();
        try (ResultSet rs = connection.getMetaData()
            .getTables(catalogName, schemaName, "%", new String[]{"TABLE"})) {
          while (rs.next()) {
            result.add(MetadataTableResponse.builder()
                .name(rs.getString("TABLE_NAME"))
                .remarks(readRemarksSafely(rs))
                .build());
          }
        }
      } catch (Exception e) {
        log.warn("Read tables by JDBC metadata failed for {} schema [{}]:{}",
            productType, schema, e.getMessage());
      }
      if (result.isEmpty()) {
        for (String name : queryNames(connection,
            MetadataCommentSql.tableNames(productType), schema)) {
          result.add(MetadataTableResponse.builder().name(name).build());
        }
        if (!result.isEmpty()) {
          log.warn("JDBC getTables returned empty for {} schema [{}], "
                  + "fallback to data dictionary: {} tables",
              productType, schema, result.size());
        }
      }
      fillTableRemarksIfMissing(connection, productType, schema, result);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  public List<MetadataTableResponse> getSchemaViews(Long id, String schema) {
    DataSourceEntity dataSourceEntity = dataSourceDao.getById(id);
    File driverPathFile = SpringUtil.getBean(DriverLoadService.class)
        .getVersionDriverFile(dataSourceEntity.getType(),
            dataSourceEntity.getVersion());
    String driverPath = driverPathFile.getAbsolutePath();
    ProductTypeEnum productType = dataSourceEntity.getType();
    if (productType.isNoViewTables()) {
      return Collections.emptyList();
    }
    List<MetadataTableResponse> result = new ArrayList<>();
    HikariDataSource ds = DataSourceUtils.getHikariDataSource(dataSourceEntity, driverPath);
    try (Connection connection = ds.getConnection()) {
      try {
        String catalogName = productType.getContext().isHasCatalogAndSchema()
            ? connection.getCatalog()
            : getAdapter(productType, connection, schema).getLeft();
        String schemaName = getAdapter(productType, connection, schema).getRight();
        try (ResultSet rs = connection.getMetaData()
            .getTables(catalogName, schemaName, "%", new String[]{"VIEW"})) {
          while (rs.next()) {
            result.add(MetadataTableResponse.builder()
                .name(rs.getString("TABLE_NAME"))
                .remarks(readRemarksSafely(rs))
                .build());
          }
        }
      } catch (Exception e) {
        log.warn("Read views by JDBC metadata failed for {} schema [{}]:{}",
            productType, schema, e.getMessage());
      }
      if (result.isEmpty()) {
        for (String name : queryNames(connection,
            MetadataCommentSql.viewNames(productType), schema)) {
          result.add(MetadataTableResponse.builder().name(name).build());
        }
        if (!result.isEmpty()) {
          log.warn("JDBC getTables(VIEW) returned empty for {} schema [{}], "
                  + "fallback to data dictionary: {} views",
              productType, schema, result.size());
        }
      }
      fillTableRemarksIfMissing(connection, productType, schema, result);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  public List<MetadataColumnResponse> getTableColumns(Long id, String schema, String table) {
    DataSourceEntity dataSourceEntity = dataSourceDao.getById(id);
    File driverPathFile = SpringUtil.getBean(DriverLoadService.class)
        .getVersionDriverFile(dataSourceEntity.getType(),
            dataSourceEntity.getVersion());
    String driverPath = driverPathFile.getAbsolutePath();
    ProductTypeEnum productType = dataSourceEntity.getType();
    HikariDataSource ds = DataSourceUtils.getHikariDataSource(dataSourceEntity, driverPath);
    try (Connection connection = ds.getConnection()) {
      return readColumns(connection, productType,
          resolveTableRef(connection, productType, schema, table));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public List<MetadataTableResponse> getTableMetadata(Long id, String schema, List<String> tables) {
    DataSourceEntity dataSourceEntity = dataSourceDao.getById(id);
    File driverPathFile = SpringUtil.getBean(DriverLoadService.class)
        .getVersionDriverFile(dataSourceEntity.getType(),
            dataSourceEntity.getVersion());
    String driverPath = driverPathFile.getAbsolutePath();
    ProductTypeEnum productType = dataSourceEntity.getType();
    List<MetadataTableResponse> result = new ArrayList<>();
    if (CollectionUtils.isEmpty(tables)) {
      return result;
    }
    HikariDataSource ds = DataSourceUtils.getHikariDataSource(dataSourceEntity, driverPath);
    try (Connection connection = ds.getConnection()) {
      Map<String, Map<String, String>> remarksCache = new HashMap<>();
      for (String token : tables) {
        String[] ref = resolveTableRef(connection, productType, schema, token);
        if (StringUtils.isBlank(ref[2])) {
          continue;
        }
        result.add(MetadataTableResponse.builder()
            .name(token.trim())
            .remarks(readTableRemarks(connection, productType, ref, remarksCache))
            .columns(readColumns(connection, productType, ref))
            .build());
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  private String[] resolveTableRef(Connection connection, ProductTypeEnum productType,
      String schema, String token) {
    String table = StringUtils.trimToEmpty(token);
    String tableSchema = StringUtils.trimToNull(schema);
    if (StringUtils.contains(table, ".")) {
      int idx = table.lastIndexOf('.');
      tableSchema = StringUtils.trimToNull(table.substring(0, idx));
      table = table.substring(idx + 1);
    }
    if (null == tableSchema) {
      tableSchema = getDefaultSchema(connection);
    }
    String catalog = productType.getContext().isHasCatalogAndSchema()
        ? getCatalog(connection)
        : getAdapter(productType, connection, tableSchema).getLeft();
    return new String[]{catalog, getAdapter(productType, connection, tableSchema).getRight(),
        table, tableSchema};
  }

  private String getDefaultSchema(Connection connection) {
    try {
      String schema = connection.getSchema();
      if (StringUtils.isNotBlank(schema)) {
        return schema;
      }
    } catch (Exception ignored) {
      // 部分驱动未实现 Connection#getSchema
    }
    try {
      String catalog = connection.getCatalog();
      if (StringUtils.isNotBlank(catalog)) {
        return catalog;
      }
    } catch (Exception ignored) {
      // 使用数据库用户名作为兜底 schema
    }
    try {
      return connection.getMetaData().getUserName();
    } catch (Exception e) {
      return null;
    }
  }

  private String getCatalog(Connection connection) {
    try {
      return connection.getCatalog();
    } catch (Exception e) {
      return null;
    }
  }

  private List<MetadataColumnResponse> readColumns(Connection connection,
      ProductTypeEnum productType, String[] ref) {
    List<MetadataColumnResponse> columns = new ArrayList<>();
    try (ResultSet rs = connection.getMetaData()
        .getColumns(ref[0], ref[1], ref[2], null)) {
      while (rs.next()) {
        columns.add(MetadataColumnResponse.builder()
            .name(rs.getString("COLUMN_NAME"))
            .type(rs.getString("TYPE_NAME"))
            .remarks(readRemarksSafely(rs))
            .build());
      }
    } catch (Exception e) {
      log.warn("Read columns metadata failed:{}", e.getMessage());
    }
    if (columns.isEmpty()) {
      columns.addAll(queryColumns(connection,
          MetadataCommentSql.columns(productType), ref[3], ref[2]));
      if (!columns.isEmpty()) {
        log.warn("JDBC getColumns returned empty for {} table [{}], "
                + "fallback to data dictionary: {} columns",
            productType, ref[2], columns.size());
      }
    }
    fillColumnRemarksIfMissing(connection, productType, ref[3], ref[2], columns);
    return columns;
  }

  private String readTableRemarks(Connection connection, ProductTypeEnum productType,
      String[] ref, Map<String, Map<String, String>> cache) {
    try (ResultSet rs = connection.getMetaData()
        .getTables(ref[0], ref[1], ref[2], new String[]{"TABLE", "VIEW"})) {
      while (rs.next()) {
        String remark = readRemarksSafely(rs);
        if (StringUtils.isNotBlank(remark)) {
          return remark.trim();
        }
      }
    } catch (Exception e) {
      log.warn("Read table remarks failed:{}", e.getMessage());
    }
    String cacheKey = productType.name() + "|" + StringUtils.defaultString(ref[3]);
    Map<String, String> comments = cache.get(cacheKey);
    if (null == comments) {
      comments = queryComments(connection,
          MetadataCommentSql.tableComments(productType), ref[3]);
      cache.put(cacheKey, comments);
    }
    return comments.get(StringUtils.upperCase(ref[2]));
  }

  private void fillTableRemarksIfMissing(Connection connection, ProductTypeEnum productType,
      String schema, List<MetadataTableResponse> tables) {
    if (CollectionUtils.isEmpty(tables)) {
      return;
    }
    boolean missingRemarks = tables.stream()
        .anyMatch(table -> StringUtils.isBlank(table.getRemarks()));
    if (!missingRemarks) {
      return;
    }
    String effectiveSchema = StringUtils.isNotBlank(schema) ? schema : getDefaultSchema(connection);
    Map<String, String> comments = queryComments(connection,
        MetadataCommentSql.tableComments(productType), effectiveSchema);
    if (comments.isEmpty()) {
      return;
    }
    for (MetadataTableResponse table : tables) {
      String remark = comments.get(StringUtils.upperCase(table.getName()));
      if (StringUtils.isNotBlank(remark)) {
        table.setRemarks(remark);
      }
    }
  }

  private void fillColumnRemarksIfMissing(Connection connection, ProductTypeEnum productType,
      String schema, String table, List<MetadataColumnResponse> columns) {
    if (CollectionUtils.isEmpty(columns)) {
      return;
    }
    boolean missingRemarks = columns.stream()
        .anyMatch(column -> StringUtils.isBlank(column.getRemarks()));
    if (!missingRemarks) {
      return;
    }
    Map<String, String> comments = queryComments(connection,
        MetadataCommentSql.columnComments(productType), schema, table);
    if (comments.isEmpty()) {
      return;
    }
    for (MetadataColumnResponse column : columns) {
      String remark = comments.get(StringUtils.upperCase(column.getName()));
      if (StringUtils.isNotBlank(remark)) {
        column.setRemarks(remark);
      }
    }
  }

  private Map<String, String> queryComments(Connection connection,
      List<MetadataCommentSql.Query> candidates, String... params) {
    if (CollectionUtils.isEmpty(candidates)) {
      return Collections.emptyMap();
    }
    for (MetadataCommentSql.Query candidate : candidates) {
      Map<String, String> comments = new LinkedHashMap<>();
      try (PreparedStatement statement = connection.prepareStatement(candidate.getSql())) {
        if (!bindParameters(statement, candidate, params)) {
          continue;
        }
        try (ResultSet rs = statement.executeQuery()) {
          while (rs.next()) {
            String name = rs.getString(1);
            String remark = rs.getString(2);
            if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(remark)) {
              String key = StringUtils.upperCase(name);
              if (!comments.containsKey(key)) {
                comments.put(key, remark.trim());
              }
            }
          }
        }
        if (!comments.isEmpty()) {
          return comments;
        }
      } catch (Exception e) {
        log.warn("Read comments from system tables failed [{}]:{}",
            candidate.getSql(), e.getMessage());
      }
    }
    return Collections.emptyMap();
  }

  private List<String> queryNames(Connection connection,
      List<MetadataCommentSql.Query> candidates, String... params) {
    if (CollectionUtils.isEmpty(candidates)) {
      return Collections.emptyList();
    }
    for (MetadataCommentSql.Query candidate : candidates) {
      Set<String> names = new LinkedHashSet<>();
      try (PreparedStatement statement = connection.prepareStatement(candidate.getSql())) {
        if (!bindParameters(statement, candidate, params)) {
          continue;
        }
        try (ResultSet rs = statement.executeQuery()) {
          while (rs.next()) {
            String name = rs.getString(1);
            if (StringUtils.isNotBlank(name)) {
              names.add(name.trim());
            }
          }
        }
        if (!names.isEmpty()) {
          log.info("Read {} object names by [{}]", names.size(), candidate.getSql());
          return new ArrayList<>(names);
        }
      } catch (Exception e) {
        log.warn("Read object names from system tables failed [{}]:{}",
            candidate.getSql(), e.getMessage());
      }
    }
    return Collections.emptyList();
  }

  private List<MetadataColumnResponse> queryColumns(Connection connection,
      List<MetadataCommentSql.Query> candidates, String... params) {
    if (CollectionUtils.isEmpty(candidates)) {
      return Collections.emptyList();
    }
    for (MetadataCommentSql.Query candidate : candidates) {
      List<MetadataColumnResponse> columns = new ArrayList<>();
      try (PreparedStatement statement = connection.prepareStatement(candidate.getSql())) {
        if (!bindParameters(statement, candidate, params)) {
          continue;
        }
        try (ResultSet rs = statement.executeQuery()) {
          while (rs.next()) {
            columns.add(MetadataColumnResponse.builder()
                .name(rs.getString(1))
                .type(rs.getString(2))
                .build());
          }
        }
        if (!columns.isEmpty()) {
          return columns;
        }
      } catch (Exception e) {
        log.warn("Read columns from system tables failed [{}]:{}",
            candidate.getSql(), e.getMessage());
      }
    }
    return Collections.emptyList();
  }

  private boolean bindParameters(PreparedStatement statement,
      MetadataCommentSql.Query candidate, String... params) throws SQLException {
    int[] indexes = candidate.getParamIndexes();
    for (int i = 0; i < indexes.length; i++) {
      int index = indexes[i];
      if (index < 0 || index >= params.length) {
        return false;
      }
      String value = params[index];
      if (null == value) {
        statement.setNull(i + 1, java.sql.Types.VARCHAR);
      } else {
        statement.setObject(i + 1, value);
      }
    }
    return true;
  }

  private List<String> distinct(List<String> names) {
    Set<String> unique = new LinkedHashSet<>();
    for (String name : names) {
      if (StringUtils.isNotBlank(name)) {
        unique.add(name.trim());
      }
    }
    return new ArrayList<>(unique);
  }

  private String readRemarksSafely(ResultSet rs) {
    try {
      return rs.getString("REMARKS");
    } catch (Exception e) {
      return null;
    }
  }

  private void validJdbcUrlFormat(DataSourceEntity conn) {
    String typeName = conn.getType().getName().toUpperCase();
    ProductTypeEnum supportDbType = ProductTypeEnum.valueOf(typeName);
    if (!conn.getUrl().startsWith(supportDbType.getUrlPrefix())) {
      throw new CommonException(ResponseErrorCode.ERROR_INVALID_JDBC_URL, conn.getUrl());
    }
    if (ProductTypeEnum.HTTP == supportDbType) {
      return;
    }

    for (int i = 0; i < supportDbType.getUrl().length; ++i) {
      String pattern = supportDbType.getUrl()[i];
      Matcher matcher = JdbcUrlUtils.getPattern(pattern).matcher(conn.getUrl());
      if (!matcher.matches()) {
        if (i == supportDbType.getUrl().length - 1) {
          throw new CommonException(ResponseErrorCode.ERROR_INVALID_JDBC_URL, conn.getUrl());
        }
      } else {
        if (supportDbType.hasDatabaseName() && StringUtils.isBlank(matcher.group("database"))) {
          throw new CommonException(ResponseErrorCode.ERROR_INVALID_JDBC_URL,
              "datasource.db.name.missing", conn.getUrl());
        }
        if (supportDbType.hasFilePath() && StringUtils.isBlank(matcher.group("file"))) {
          throw new CommonException(ResponseErrorCode.ERROR_INVALID_JDBC_URL,
              "datasource.file.path.missing", conn.getUrl());
        }

        break;
      }
    }
  }

  private Pair<String, String> getAdapter(ProductTypeEnum productType, Connection connection, String schema) {
    if (null != productType.getContext().getAdapter()) {
      return productType.getContext().getAdapter().apply(schema);
    }
    return getAdapter(connection, schema);
  }

  @SneakyThrows
  private Pair<String, String> getAdapter(Connection connection, String schema) {
    boolean hasCatalogLayer = hasCatalogLayer(connection);
    boolean hasSchemaLayer = hasSchemaLayer(connection);
    if (hasCatalogLayer) {
      if (hasSchemaLayer) {
        return Pair.of(connection.getCatalog(), schema);
      } else {
        return Pair.of(schema, null);
      }
    } else {
      return Pair.of(null, schema);
    }
  }

  private boolean hasCatalogLayer(Connection conn) {
    try (ResultSet catalogs = conn.getMetaData().getCatalogs()) {
      return catalogs.next();
    } catch (SQLException e) {
      throw new RuntimeException("Detect has catalog layer failed:" + e.getMessage());
    }
  }

  private boolean hasSchemaLayer(Connection conn) {
    try {
      DatabaseMetaData dbMeta = conn.getMetaData();
      try (ResultSet catalogRs = dbMeta.getCatalogs()) {
        while (catalogRs.next()) {
          String name = catalogRs.getString("TABLE_CAT");
          try (ResultSet schemaRs = dbMeta.getSchemas(name, null)) {
            if (schemaRs.next()) {
              return true;
            }
          }
        }
      }
      try (ResultSet schemaRs = dbMeta.getSchemas()) {
        return schemaRs.next();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Detect has schema layer failed:" + e.getMessage());
    }
  }

}
