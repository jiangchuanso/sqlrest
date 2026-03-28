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

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.dromara.sqlrest.persistence.entity.PoolConfig;
import org.dromara.sqlrest.persistence.util.JsonUtils;

/**
 * PoolConfig JSON 类型处理器
 */
@Slf4j
public class PoolConfigHandler extends BaseTypeHandler<PoolConfig> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, PoolConfig parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setString(i, JsonUtils.toJsonString(parameter));
  }

  @Override
  public PoolConfig getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return parseJson(rs.getString(columnName));
  }

  @Override
  public PoolConfig getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return parseJson(rs.getString(columnIndex));
  }

  @Override
  public PoolConfig getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    return parseJson(cs.getString(columnIndex));
  }

  private PoolConfig parseJson(String json) {
    if (json == null || json.isEmpty()) {
      return PoolConfig.DEFAULT;
    }
    return JsonUtils.toBeanObject(json, PoolConfig.class);
  }
}
