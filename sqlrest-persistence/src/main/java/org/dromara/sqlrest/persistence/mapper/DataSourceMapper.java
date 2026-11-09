// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Select;
import org.dromara.sqlrest.common.dto.NameCount;
import org.dromara.sqlrest.persistence.entity.DataSourceEntity;

public interface DataSourceMapper extends BaseMapper<DataSourceEntity> {

  @Select("SELECT type as name,count(1) as count from SQLREST_DATASOURCE GROUP BY type ORDER BY count DESC")
  List<NameCount> getDatasourceTypeRatio();
}
