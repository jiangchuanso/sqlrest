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
import org.dromara.sqlrest.persistence.entity.ApiAssignmentEntity;
import org.dromara.sqlrest.persistence.entity.ModuleAssignmentEntity;

public interface ApiAssignmentMapper extends BaseMapper<ApiAssignmentEntity> {

  @Select("<script>"
      + "<if test='_databaseId == \"mysql\" '>"
      + "  SELECT id from SQLREST_API_ASSIGNMENT a  "
      + "  WHERE status = 1 and NOT EXISTS ( "
      + "   SELECT 1 from SQLREST_API_ONLINE o "
      + "   WHERE o.method=a.method and o.path=a.path "
      + "  )"
      + "</if>"
      + "<if test='_databaseId == \"postgresql\" '>"
      + "  SELECT id from SQLREST_API_ASSIGNMENT a  "
      + "  WHERE status = true and NOT EXISTS ( "
      + "   SELECT 1 from SQLREST_API_ONLINE o "
      + "   WHERE o.method=a.method and o.path=a.path "
      + "  )"
      + "</if>"
      + "</script>")
  List<Long> getUpgradeOnlineAssignments();

  @Update("<script>"
      + "<if test='_databaseId == \"mysql\" '>"
      + " UPDATE SQLREST_API_ASSIGNMENT "
      + " SET status = 0 "
      + " WHERE id IN "
      + "<foreach collection='ids' item='item' open='(' separator=',' close=')'> "
      + "   #{item} "
      + "</foreach>"
      + "</if>"
      + "<if test='_databaseId == \"postgresql\" '>"
      + " UPDATE SQLREST_API_ASSIGNMENT "
      + " SET status = false "
      + " WHERE id IN "
      + "<foreach collection='ids' item='item' open='(' separator=',' close=')'> "
      + "   #{item} "
      + "</foreach>"
      + "</if>"
      + "</script>")
  void resetUpgradeOnlineAssignments(@Param("ids") List<Long> ids);

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
      + "UPDATE SQLREST_API_ASSIGNMENT SET group_id = #{groupId} WHERE id IN "
      + "<foreach collection='ids' item='item' open='(' separator=',' close=')'> "
      + "   #{item} "
      + "</foreach>"
      + "</script>")
  void updateGroup(@Param("groupId") Long groupId, @Param("ids") List<Long> ids);

  @Select("<script>"
      + "SELECT * "
      + "FROM SQLREST_API_ASSIGNMENT a  WHERE 1=1 "
      + "<if test='groupId != null '>"
      + " AND a.group_id = #{groupId} "
      + "</if>"
      + "<if test='moduleId != null '>"
      + " AND a.module_id = #{moduleId} "
      + "</if>"
      + "<if test='open != null '>"
      + " AND a.open = #{open} "
      + "</if>"
      + "<if test='online != null '>"
      + " AND not exists (SELECT 1 FROM SQLREST_API_ONLINE o where o.api_id=a.id)"
      + "</if>"
      + "<if test='searchText != null and searchText.length()>0 '>"
      + " AND a.name like #{searchText,jdbcType=VARCHAR} "
      + "</if>"
      + " ORDER BY a.id desc "
      + "</script>")
  List<ApiAssignmentEntity> searchAll(@Param("groupId") Long groupId, @Param("moduleId") Long moduleId,
      @Param("open") Boolean open, @Param("searchText") String searchText, @Param("online") Boolean online);
}
