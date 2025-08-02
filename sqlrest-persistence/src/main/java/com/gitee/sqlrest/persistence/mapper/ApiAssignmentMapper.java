// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.sqlrest.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gitee.sqlrest.persistence.entity.ApiAssignmentEntity;
import com.gitee.sqlrest.persistence.entity.ModuleAssignmentEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface ApiAssignmentMapper extends BaseMapper<ApiAssignmentEntity> {

  @Select("<script>"
      + "SELECT "
      + " a.id as assigment_id,"
      + " a.name as assigment_name,"
      + " a.group_id as group_id,"
      + " a.module_id as module_id,"
      + " m.name as module_name "
      + "FROM SQLREST_API_ASSIGNMENT a "
      + "LEFT JOIN SQLREST_API_MODULE m on a.module_id = m.id"
      + "</script>")
  List<ModuleAssignmentEntity> getModuleAssignments();

  @Update("UPDATE SQLREST_API_ASSIGNMENT SET group_id = 1 WHERE group_id=#{groupId}")
  void resetGroup(@Param("groupId") Long groupId);

  @Update("<script>"
      + "UPDATE SQLREST_API_ASSIGNMENT SET group_id = #{groupId} WHERE id in "
      + "<foreach collection='ids' item='item' open='(' separator=',' close=')'> "
      + "   #{item} "
      + "</foreach>"
      + "</script>")
  void updateGroup(@Param("groupId") Long groupId, @Param("ids") List<Long> ids);
}
