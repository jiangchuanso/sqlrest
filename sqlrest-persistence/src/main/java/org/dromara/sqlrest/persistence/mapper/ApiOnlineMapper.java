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
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dromara.sqlrest.common.dto.ApiIdVersion;
import org.dromara.sqlrest.persistence.entity.ApiOnlineEntity;

public interface ApiOnlineMapper extends BaseMapper<ApiOnlineEntity> {

  @Select("<script>"
      + " SELECT api_id, commit_id, version FROM SQLREST_API_ONLINE "
      + " WHERE api_id IN "
      + "<foreach collection='apiIds' item='item' open='(' separator=',' close=')'> "
      + "   #{item} "
      + "</foreach>"
      + "</script>")
  List<ApiIdVersion> filterOnline(@Param("apiIds") List<Long> apiIds);

  @Update("UPDATE SQLREST_API_ONLINE SET group_id = 1 WHERE group_id=#{groupId}")
  void resetGroup(@Param("groupId") Long groupId);

  @Update("<script>"
      + "UPDATE SQLREST_API_ONLINE SET group_id = #{groupId} WHERE api_id IN "
      + "<foreach collection='apiIds' item='item' open='(' separator=',' close=')'> "
      + "   #{item} "
      + "</foreach>"
      + "</script>")
  void updateGroup(@Param("groupId") Long groupId, @Param("apiIds") List<Long> apiIds);
}
