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

import com.fasterxml.jackson.core.type.TypeReference;
import org.dromara.sqlrest.persistence.util.JsonUtils;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

public class ParamMapHandler extends BaseTypeHandler<Map<String, Object>> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, Map<String, Object> value, JdbcType jdbcType)
      throws SQLException {
    ps.setString(i, map2string(value));
  }

  @Override
  public Map<String, Object> getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return string2map(rs.getString(columnName));
  }

  @Override
  public Map<String, Object> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return string2map(rs.getString(columnIndex));
  }

  @Override
  public Map<String, Object> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    return string2map(cs.getString(columnIndex));
  }

  private String map2string(Map<String, Object> map) {
    if (map == null || map.isEmpty()) {
      return null;
    }
    return JsonUtils.toJsonString(map);
  }

  private Map<String, Object> string2map(String str) {
    if (str == null || str.isEmpty()) {
      return new HashMap<>(2);
    }
    return JsonUtils.toBeanType(str, new TypeReference<Map<String, Object>>() {
    });
  }
}
