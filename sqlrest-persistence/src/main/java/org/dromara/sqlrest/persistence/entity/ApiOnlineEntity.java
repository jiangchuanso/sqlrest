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
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.EnumTypeHandler;
import org.dromara.sqlrest.common.enums.HttpMethodEnum;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "SQLREST_API_ONLINE", autoResultMap = true)
public class ApiOnlineEntity {

  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  @TableField("name")
  private String name;

  @TableField(value = "method", typeHandler = EnumTypeHandler.class)
  private HttpMethodEnum method;

  @TableField("path")
  private String path;

  @TableField("api_id")
  private Long apiId;

  @TableField("group_id")
  private Long groupId;

  @TableField("module_id")
  private Long moduleId;

  @TableField("datasource_id")
  private Long datasourceId;

  @TableField("open")
  private Boolean open;

  @TableField("alarm")
  private Boolean alarm;
  
  @TableField("flow_status")
  private Boolean flowStatus;

  @TableField("commit_id")
  private Long commitId;

  @TableField("version")
  private Integer version;

  @TableField("content")
  private String content;

  @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
  private Timestamp createTime;

  @TableField(value = "update_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
  private Timestamp updateTime;
}
