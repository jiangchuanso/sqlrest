// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.persistence.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.EnumTypeHandler;
import org.dromara.sqlrest.common.dto.ItemParam;
import org.dromara.sqlrest.common.dto.OutParam;
import org.dromara.sqlrest.common.enums.CacheKeyTypeEnum;
import org.dromara.sqlrest.common.enums.DataTypeFormatEnum;
import org.dromara.sqlrest.common.enums.ExecuteEngineEnum;
import org.dromara.sqlrest.common.enums.HttpMethodEnum;
import org.dromara.sqlrest.common.enums.NamingStrategyEnum;
import org.dromara.sqlrest.persistence.handler.FormatMapHandler;
import org.dromara.sqlrest.persistence.handler.ListOutputHandler;
import org.dromara.sqlrest.persistence.handler.ListParamHandler;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "SQLREST_API_ASSIGNMENT", autoResultMap = true)
public class ApiAssignmentEntity {

  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  @TableField("group_id")
  private Long groupId;

  @TableField("module_id")
  private Long moduleId;

  @TableField("datasource_id")
  private Long datasourceId;

  @TableField("name")
  private String name;

  @TableField("description")
  private String description;

  @TableField(value = "method", typeHandler = EnumTypeHandler.class)
  private HttpMethodEnum method;

  @TableField("path")
  private String path;

  @TableField(value = "params", typeHandler = ListParamHandler.class)
  private List<ItemParam> params;

  @TableField(value = "outputs", typeHandler = ListOutputHandler.class)
  private List<OutParam> outputs;

  @TableField("open")
  private Boolean open;

  @TableField("alarm")
  private Boolean alarm;

  @TableField("content_type")
  private String contentType;

  @TableField(value = "engine", typeHandler = EnumTypeHandler.class)
  private ExecuteEngineEnum engine;

  @TableField(value = "response_format", typeHandler = FormatMapHandler.class)
  private Map<DataTypeFormatEnum, String> responseFormat;

  @TableField(value = "naming_strategy", typeHandler = EnumTypeHandler.class)
  private NamingStrategyEnum namingStrategy;

  @TableField("flow_status")
  private Boolean flowStatus;

  @TableField("flow_grade")
  private Integer flowGrade;

  @TableField("flow_count")
  private Integer flowCount;

  @TableField(value = "cache_key_type", typeHandler = EnumTypeHandler.class)
  private CacheKeyTypeEnum cacheKeyType;

  @TableField("cache_key_expr")
  private String cacheKeyExpr;

  @TableField("cache_expire_seconds")
  private Long cacheExpireSeconds;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
  private Timestamp createTime;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
  private Timestamp updateTime;

  @TableField(exist = false)
  private List<ApiContextEntity> contextList;
}
