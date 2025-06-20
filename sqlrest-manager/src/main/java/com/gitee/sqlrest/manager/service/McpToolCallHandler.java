// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.sqlrest.manager.service;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gitee.sqlrest.common.dto.BaseParam;
import com.gitee.sqlrest.common.dto.ItemParam;
import com.gitee.sqlrest.common.dto.ResultEntity;
import com.gitee.sqlrest.core.exec.ApiExecuteService;
import com.gitee.sqlrest.core.util.JacksonUtils;
import com.gitee.sqlrest.persistence.entity.ApiAssignmentEntity;
import com.google.common.collect.Lists;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;

/**
 * https://mcp-docs.cn/docs/concepts/tools
 */
public class McpToolCallHandler {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final String FN_TYPE = "type";
  private static final String FN_ID = "id";
  private static final String FN_DESCRIPTION = "description";
  private static final String FN_PROPERTIES = "properties";
  private static final String FN_ITEMS = "items";
  private static final String FV_ID = "urn:jsonschema:Operation";
  private static final String FV_OBJECT = "object";
  private static final String FV_ARRAY = "array";

  private final String toolName;
  private final String toolDescription;
  private final ApiAssignmentEntity config;
  private final ApiExecuteService apiExecuteService;

  public McpToolCallHandler(String toolName, String toolDescription,
      ApiAssignmentEntity config) {
    this.toolName = toolName;
    this.toolDescription = toolDescription;
    this.config = config;
    this.apiExecuteService = SpringUtil.getBean(ApiExecuteService.class);
  }

  public McpSchema.Tool getMcpToolSchema() {
    List<ItemParam> params = config.getParams();
    ObjectNode rootNode = objectMapper.createObjectNode();
    rootNode.put(FN_TYPE, FV_OBJECT);
    rootNode.put(FN_ID, FV_ID);
    ObjectNode propertiesNode = objectMapper.createObjectNode();
    for (ItemParam param : params) {
      ObjectNode node = objectMapper.createObjectNode();
      if (param.getIsArray()) {
        ObjectNode items = objectMapper.createObjectNode();
        items.put(FN_TYPE, param.getType().getJsType());
        node.put(FN_TYPE, FV_ARRAY);
        node.put(FN_DESCRIPTION, param.getRemark());
        node.set(FN_ITEMS, items);
      } else {
        node.put(FN_TYPE, param.getType().getJsType());
        node.put(FN_DESCRIPTION, param.getRemark());
        if (CollectionUtils.isNotEmpty(param.getChildren())) {
          ObjectNode properties = objectMapper.createObjectNode();
          for (BaseParam subParam : param.getChildren()) {
            ObjectNode item = objectMapper.createObjectNode();
            item.put(FN_TYPE, subParam.getType().getJsType());
            item.put(FN_DESCRIPTION, subParam.getRemark());
            properties.set(subParam.getName(), item);
          }
          node.set(FN_PROPERTIES, properties);
        }
      }
      propertiesNode.set(param.getName(), node);
    }
    rootNode.set(FN_PROPERTIES, propertiesNode);

    try {
      String schema = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
      return new McpSchema.Tool(toolName, toolDescription, schema);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

  }

  public CallToolResult executeTool(McpSyncServerExchange exchange, Map<String, Object> arguments) {
    ResultEntity<Object> resultEntity = apiExecuteService.execute(config, arguments);
    if (0 == resultEntity.getCode()) {
      String json = JacksonUtils.toJsonStr(resultEntity.getData(), config.getResponseFormat());
      McpSchema.TextContent content = new McpSchema.TextContent("获取JSON格式的数据为:\n " + json);
      return new McpSchema.CallToolResult(Lists.newArrayList(content), false);
    } else {
      String message = resultEntity.getMessage();
      McpSchema.TextContent content = new McpSchema.TextContent("获取数据异常:\n " + message);
      return new McpSchema.CallToolResult(Lists.newArrayList(content), true);
    }
  }
}
