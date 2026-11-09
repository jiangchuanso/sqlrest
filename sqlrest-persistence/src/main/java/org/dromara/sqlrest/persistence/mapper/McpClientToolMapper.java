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
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.sqlrest.common.dto.IdWithName;
import org.dromara.sqlrest.persistence.entity.McpClientToolEntity;

public interface McpClientToolMapper extends BaseMapper<McpClientToolEntity> {

  @Select("SELECT t.id AS id, t.name AS name "
      + "FROM SQLREST_MCP_CLIENT_TOOL a "
      + "JOIN SQLREST_MCP_TOOL t ON a.tool_id = t.id "
      + "WHERE a.client_id = #{clientId}")
  List<IdWithName> getAuthTool(@Param("clientId") Long clientId);

  @Select("SELECT t.name AS name "
      + "FROM SQLREST_MCP_CLIENT_TOOL a "
      + "JOIN SQLREST_MCP_CLIENT c ON a.client_id = c.id "
      + "JOIN SQLREST_MCP_TOOL t   ON a.tool_id = t.id "
      + "WHERE c.token = #{token}")
  List<String> listAllowedToolNames(@Param("token") String token);

  @Insert("<script>"
      + "INSERT INTO SQLREST_MCP_CLIENT_TOOL(client_id,tool_id) VALUES "
      + "<foreach collection='entities' item='item' separator=',' > "
      + "  ( #{item.clientId} ,#{item.toolId} ) "
      + "</foreach>"
      + "</script>")
  void insertList(@Param("entities") List<McpClientToolEntity> entities);
}
