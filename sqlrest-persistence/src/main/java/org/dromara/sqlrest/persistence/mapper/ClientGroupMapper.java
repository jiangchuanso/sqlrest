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
import org.dromara.sqlrest.common.dto.IdWithName;
import org.dromara.sqlrest.persistence.entity.ClientGroupEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ClientGroupMapper extends BaseMapper<ClientGroupEntity> {

  @Select("SELECT g.id,g.name from SQLREST_CLIENT_GROUP a LEFT JOIN SQLREST_API_GROUP g on a.group_id=g.id where a.client_id= #{id}")
  List<IdWithName> getGroupAuth(@Param("id") Long id);
}
