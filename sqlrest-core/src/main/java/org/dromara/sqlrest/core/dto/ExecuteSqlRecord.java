// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.dto;

import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.reflection.ArrayUtil;
import org.dromara.sqlrest.common.service.DisplayRecord;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteSqlRecord implements DisplayRecord {

  private String sql;

  private List<Object> parameter;

  private Long costs;


  protected String getParameterValueString() {
    List<Object> typeList = new ArrayList<>(parameter.size());
    for (Object value : parameter) {
      if (value == null) {
        typeList.add("null");
      } else {
        typeList.add(objectValueString(value) + "(" + value.getClass().getSimpleName() + ")");
      }
    }
    final String parameters = typeList.toString();
    return parameters.substring(1, parameters.length() - 1);
  }

  protected String objectValueString(Object value) {
    if (value instanceof Array) {
      try {
        return ArrayUtil.toString(((Array) value).getArray());
      } catch (SQLException e) {
        return value.toString();
      }
    }
    return value.toString();
  }

  @Override
  public String getDisplayText() {
    return "==>   Preparing: " + sql.trim() + "\n"
        + "==>  Parameters: " + getParameterValueString() + "\n"
        + "==>       costs: " + costs + " ms";
  }
}
