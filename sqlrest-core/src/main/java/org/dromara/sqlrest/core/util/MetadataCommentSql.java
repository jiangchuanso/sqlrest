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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.dromara.sqlrest.common.enums.ProductTypeEnum;

/**
 * 部分 JDBC 驱动的 DatabaseMetaData 不返回 REMARKS，此处按数据库类型提供
 * 数据字典(系统表)兜底查询。所有注释 SQL 均返回两列：对象名、注释。
 * <p>
 * 达梦(DM)的 JDBC 元数据对 schema 大小写敏感且带权限过滤，生产环境常出现
 * getTables()/getColumns() 返回空、ALL_* 兼容视图无数据的情况。因此 DM 额外提供
 * 表/视图/列清单的兜底 SQL，并按「ALL_* -> USER_* -> SYS 原生系统表」顺序逐个尝试，
 * 任一候选查到数据即返回。
 * </p>
 * {@link Query#getParamIndexes()} 指定每个 ? 取调用方参数数组中的哪个下标，
 * 以便无 OWNER 条件的 USER_* 视图复用同一组参数。
 */
public final class MetadataCommentSql {

  private static final String MYSQL_TABLES =
      "SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = ?";
  private static final String MYSQL_COLUMNS =
      "SELECT COLUMN_NAME, COLUMN_COMMENT FROM information_schema.COLUMNS "
          + "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
  private static final String ORACLE_TABLES =
      "SELECT TABLE_NAME, COMMENTS FROM ALL_TAB_COMMENTS "
          + "WHERE OWNER = ? AND TABLE_TYPE IN ('TABLE','VIEW')";
  private static final String ORACLE_COLUMNS =
      "SELECT COLUMN_NAME, COMMENTS FROM ALL_COL_COMMENTS WHERE OWNER = ? AND TABLE_NAME = ?";
  private static final String PG_TABLES =
      "SELECT c.relname, obj_description(c.oid) FROM pg_class c "
          + "JOIN pg_namespace n ON n.oid = c.relnamespace "
          + "WHERE n.nspname = ? AND c.relkind IN ('r','v','m','p','f')";
  private static final String PG_COLUMNS =
      "SELECT a.attname, col_description(a.attrelid, a.attnum) FROM pg_attribute a "
          + "JOIN pg_class c ON c.oid = a.attrelid "
          + "JOIN pg_namespace n ON n.oid = c.relnamespace "
          + "WHERE n.nspname = ? AND c.relname = ? AND a.attnum > 0 AND NOT a.attisdropped";
  private static final String SQLSERVER_TABLES =
      "SELECT o.name, CAST(ep.value AS NVARCHAR(4000)) FROM sys.objects o "
          + "JOIN sys.schemas s ON s.schema_id = o.schema_id "
          + "LEFT JOIN sys.extended_properties ep ON ep.major_id = o.object_id "
          + "AND ep.minor_id = 0 AND ep.name = 'MS_Description' "
          + "WHERE s.name = ? AND o.type IN ('U','V')";
  private static final String SQLSERVER_COLUMNS =
      "SELECT c.name, CAST(ep.value AS NVARCHAR(4000)) FROM sys.columns c "
          + "JOIN sys.objects o ON o.object_id = c.object_id "
          + "JOIN sys.schemas s ON s.schema_id = o.schema_id "
          + "LEFT JOIN sys.extended_properties ep ON ep.major_id = c.object_id "
          + "AND ep.minor_id = c.column_id AND ep.name = 'MS_Description' "
          + "WHERE s.name = ? AND o.name = ? AND o.type IN ('U','V')";
  private static final String DB2_TABLES =
      "SELECT TABNAME, REMARKS FROM SYSCAT.TABLES WHERE TABSCHEMA = ?";
  private static final String DB2_COLUMNS =
      "SELECT COLNAME, REMARKS FROM SYSCAT.COLUMNS WHERE TABSCHEMA = ? AND TABNAME = ?";

  /** 达梦：ALL_* 兼容视图受授权影响，USER_* 仅当前用户模式，SYS 原生表最完整。 */
  private static final String DM_ALL_TABLE_COMMENTS =
      "SELECT TABLE_NAME, COMMENTS FROM ALL_TAB_COMMENTS WHERE UPPER(OWNER) = UPPER(?)";
  private static final String DM_USER_TABLE_COMMENTS =
      "SELECT TABLE_NAME, COMMENTS FROM USER_TAB_COMMENTS";
  private static final String DM_SYS_TABLE_COMMENTS =
      "SELECT TVNAME, COMMENT$ FROM SYSTABLECOMMENTS WHERE UPPER(SCHNAME) = UPPER(?)";
  private static final String DM_ALL_COL_COMMENTS =
      "SELECT COLUMN_NAME, COMMENTS FROM ALL_COL_COMMENTS "
          + "WHERE UPPER(OWNER) = UPPER(?) AND UPPER(TABLE_NAME) = UPPER(?)";
  private static final String DM_USER_COL_COMMENTS =
      "SELECT COLUMN_NAME, COMMENTS FROM USER_COL_COMMENTS WHERE UPPER(TABLE_NAME) = UPPER(?)";
  private static final String DM_SYS_COL_COMMENTS =
      "SELECT COLNAME, COMMENT$ FROM SYSCOLUMNCOMMENTS "
          + "WHERE UPPER(SCHNAME) = UPPER(?) AND UPPER(TVNAME) = UPPER(?)";

  private static final String DM_ALL_TABLES =
      "SELECT TABLE_NAME FROM ALL_TABLES WHERE UPPER(OWNER) = UPPER(?)";
  private static final String DM_USER_TABLES = "SELECT TABLE_NAME FROM USER_TABLES";
  private static final String DM_SYS_TABLES =
      "SELECT NAME FROM SYS.SYSOBJECTS WHERE TYPE$ = 'SCHOBJ' AND SUBTYPE$ = 'UTAB' "
          + "AND SCHID IN (SELECT ID FROM SYS.SYSOBJECTS "
          + "WHERE TYPE$ = 'SCH' AND UPPER(NAME) = UPPER(?))";
  private static final String DM_ALL_VIEWS =
      "SELECT VIEW_NAME FROM ALL_VIEWS WHERE UPPER(OWNER) = UPPER(?)";
  private static final String DM_USER_VIEWS = "SELECT VIEW_NAME FROM USER_VIEWS";
  private static final String DM_SYS_VIEWS =
      "SELECT NAME FROM SYS.SYSOBJECTS WHERE TYPE$ = 'SCHOBJ' AND SUBTYPE$ = 'VIEW' "
          + "AND SCHID IN (SELECT ID FROM SYS.SYSOBJECTS "
          + "WHERE TYPE$ = 'SCH' AND UPPER(NAME) = UPPER(?))";
  private static final String DM_ALL_COLUMNS =
      "SELECT COLUMN_NAME, DATA_TYPE FROM ALL_TAB_COLUMNS "
          + "WHERE UPPER(OWNER) = UPPER(?) AND UPPER(TABLE_NAME) = UPPER(?)";
  private static final String DM_USER_COLUMNS =
      "SELECT COLUMN_NAME, DATA_TYPE FROM USER_TAB_COLUMNS WHERE UPPER(TABLE_NAME) = UPPER(?)";
  private static final String DM_ALL_OWNERS =
      "SELECT DISTINCT OWNER FROM ALL_TABLES WHERE OWNER IS NOT NULL";
  private static final String DM_SYS_SCHEMAS =
      "SELECT NAME FROM SYS.SYSOBJECTS WHERE TYPE$ = 'SCH'";

  private MetadataCommentSql() {
  }

  /** 一条兜底 SQL 及其 ? 对应的参数下标（下标指向调用方传入的参数数组）。 */
  public static final class Query {

    private final String sql;
    private final int[] paramIndexes;

    private Query(String sql, int... paramIndexes) {
      this.sql = sql;
      this.paramIndexes = null == paramIndexes ? new int[0] : paramIndexes;
    }

    public String getSql() {
      return sql;
    }

    public int[] getParamIndexes() {
      return paramIndexes.clone();
    }
  }

  private static Query q(String sql, int... paramIndexes) {
    return new Query(sql, paramIndexes);
  }

  /** 表/视图注释：调用方参数数组为 [schema]。 */
  public static List<Query> tableComments(ProductTypeEnum type) {
    if (null == type) {
      return Collections.emptyList();
    }
    switch (type) {
      case MYSQL:
      case MARIADB:
      case GBASE8A:
      case DORIS:
      case STARROCKS:
      case OCEANBASE:
        return Collections.singletonList(q(MYSQL_TABLES, 0));
      case ORACLE:
        return Collections.singletonList(q(ORACLE_TABLES, 0));
      case DM:
        return Arrays.asList(q(DM_ALL_TABLE_COMMENTS, 0),
            q(DM_USER_TABLE_COMMENTS), q(DM_SYS_TABLE_COMMENTS, 0));
      case POSTGRESQL:
      case KINGBASE:
      case OPENGAUSS:
        return Collections.singletonList(q(PG_TABLES, 0));
      case SQLSERVER:
        return Collections.singletonList(q(SQLSERVER_TABLES, 0));
      case DB2:
        return Collections.singletonList(q(DB2_TABLES, 0));
      default:
        return Collections.emptyList();
    }
  }

  /** 列注释：调用方参数数组为 [schema, table]。 */
  public static List<Query> columnComments(ProductTypeEnum type) {
    if (null == type) {
      return Collections.emptyList();
    }
    switch (type) {
      case MYSQL:
      case MARIADB:
      case GBASE8A:
      case DORIS:
      case STARROCKS:
      case OCEANBASE:
        return Collections.singletonList(q(MYSQL_COLUMNS, 0, 1));
      case ORACLE:
        return Collections.singletonList(q(ORACLE_COLUMNS, 0, 1));
      case DM:
        return Arrays.asList(q(DM_ALL_COL_COMMENTS, 0, 1),
            q(DM_USER_COL_COMMENTS, 1), q(DM_SYS_COL_COMMENTS, 0, 1));
      case POSTGRESQL:
      case KINGBASE:
      case OPENGAUSS:
        return Collections.singletonList(q(PG_COLUMNS, 0, 1));
      case SQLSERVER:
        return Collections.singletonList(q(SQLSERVER_COLUMNS, 0, 1));
      case DB2:
        return Collections.singletonList(q(DB2_COLUMNS, 0, 1));
      default:
        return Collections.emptyList();
    }
  }

  /** 表清单兜底（返回 1 列）：调用方参数数组为 [schema]，仅 DM 需要。 */
  public static List<Query> tableNames(ProductTypeEnum type) {
    if (ProductTypeEnum.DM != type) {
      return Collections.emptyList();
    }
    return Arrays.asList(q(DM_ALL_TABLES, 0), q(DM_USER_TABLES), q(DM_SYS_TABLES, 0));
  }

  /** 视图清单兜底（返回 1 列）：调用方参数数组为 [schema]，仅 DM 需要。 */
  public static List<Query> viewNames(ProductTypeEnum type) {
    if (ProductTypeEnum.DM != type) {
      return Collections.emptyList();
    }
    return Arrays.asList(q(DM_ALL_VIEWS, 0), q(DM_USER_VIEWS), q(DM_SYS_VIEWS, 0));
  }

  /** 列清单兜底（返回 2 列：列名、类型）：调用方参数数组为 [schema, table]，仅 DM 需要。 */
  public static List<Query> columns(ProductTypeEnum type) {
    if (ProductTypeEnum.DM != type) {
      return Collections.emptyList();
    }
    return Arrays.asList(q(DM_ALL_COLUMNS, 0, 1), q(DM_USER_COLUMNS, 1));
  }

  /** schema 清单兜底（返回 1 列，无参数）：仅 DM 需要。 */
  public static List<Query> schemaNames(ProductTypeEnum type) {
    if (ProductTypeEnum.DM != type) {
      return Collections.emptyList();
    }
    return Arrays.asList(q(DM_ALL_OWNERS), q(DM_SYS_SCHEMAS));
  }
}
