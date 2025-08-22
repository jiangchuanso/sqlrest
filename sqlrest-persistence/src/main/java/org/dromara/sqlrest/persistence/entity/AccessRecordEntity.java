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
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dromara.sqlrest.persistence.handler.ParamMapHandler;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "SQLREST_ACCESS_RECORD", autoResultMap = true)
public class AccessRecordEntity {

  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  @TableField("path")
  private String path;

  @TableField("status")
  private Integer status;

  @TableField("duration")
  private Long duration;

  @TableField("ip_addr")
  private String ipAddr;

  @TableField("user_agent")
  private String userAgent;

  @TableField("client_key")
  private String clientKey;

  @TableField("api_id")
  private Long apiId;

  @TableField(value = "parameters", typeHandler = ParamMapHandler.class)
  private Map<String, Object> parameters;

  @TableField("exception")
  private String exception;

  @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
  private Timestamp createTime;

  @TableField("executor_addr")
  private String executorAddr;

  @TableField("gateway_addr")
  private String gatewayAddr;
}
