// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.persistence.handler;

import org.dromara.sqlrest.common.dto.OutParam;
import org.dromara.sqlrest.persistence.util.JsonUtils;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

public class ListOutputHandler extends BaseTypeHandler<List<OutParam>> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, List<OutParam> list, JdbcType jdbcType)
      throws SQLException {
    ps.setString(i, list2string(list));
  }

  @Override
  public List<OutParam> getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    String r = rs.getString(columnName);
    if (rs.wasNull()) {
      return null;
    }
    return string2list(r);
  }

  @Override
  public List<OutParam> getNullableResult(ResultSet rs, int columnIndex)
      throws SQLException {
    String r = rs.getString(columnIndex);
    if (rs.wasNull()) {
      return null;
    }
    return string2list(r);
  }

  @Override
  public List<OutParam> getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    String r = cs.getString(columnIndex);
    if (cs.wasNull()) {
      return null;
    }
    return string2list(r);
  }

  private String list2string(List<OutParam> list) {
    if (list == null || list.isEmpty()) {
      return null;
    }
    return JsonUtils.toJsonString(list);
  }

  private List<OutParam> string2list(String str) {
    if (str == null || str.isEmpty()) {
      return new ArrayList<>();
    }
    return JsonUtils.toBeanList(str, OutParam.class);
  }
}
