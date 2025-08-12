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
import org.dromara.sqlrest.persistence.entity.AppClientEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface AppClientMapper extends BaseMapper<AppClientEntity> {

  @Select("<script>"
      + " SELECT * FROM SQLREST_APP_CLIENT "
      + " WHERE id in(SELECT client_id FROM SQLREST_CLIENT_GROUP WHERE group_id = #{groupId} )"
      + "<if test='searchText != null and searchText.length()>0 '>"
      + " AND name like #{searchText,jdbcType=VARCHAR} "
      + "</if>"
      + " ORDER BY create_time desc "
      + "</script>")
  List<AppClientEntity> searchAppClient(@Param("searchText") String searchText, @Param("groupId") Long groupId);
}
