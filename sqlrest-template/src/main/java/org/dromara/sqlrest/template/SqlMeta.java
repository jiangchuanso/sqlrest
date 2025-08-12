// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.template;

import java.util.List;

public class SqlMeta {

  private String sql;
  private List<Object> parameter;

  public SqlMeta(String sql, List<Object> parameter) {
    super();
    this.sql = sql.trim();
    this.parameter = parameter;
  }

  public String getSql() {
    return sql;
  }

  public void setSql(String sql) {
    this.sql = sql.trim();
  }

  public List<Object> getParameter() {
    return parameter;
  }

  public void setParameter(List<Object> parameter) {
    this.parameter = parameter;
  }

  public boolean isQuerySQL() {
    String upperSql = sql.toUpperCase().trim();
    return upperSql.startsWith("SELECT") || upperSql.startsWith("WITH");
  }

  @Override
  public String toString() {
    return "SqlMeta [sql=" + sql + ", parameter=" + parameter + "]";
  }


}
