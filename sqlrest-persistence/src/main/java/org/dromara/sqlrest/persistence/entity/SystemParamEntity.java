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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.dromara.sqlrest.common.enums.ParamTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "SQLREST_SYSTEM_PARAM", autoResultMap = true)
public class SystemParamEntity {

  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  @TableField("param_key")
  private String paramKey;

  @TableField("param_type")
  private ParamTypeEnum paramType;

  @TableField("param_value")
  private String paramValue;
}
