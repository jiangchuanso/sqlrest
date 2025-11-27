// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.dto;

import java.util.List;
import java.util.Map;
import lombok.Data;
import org.dromara.sqlrest.common.dto.ItemParam;
import org.dromara.sqlrest.common.dto.OutParam;
import org.dromara.sqlrest.common.enums.CacheKeyTypeEnum;
import org.dromara.sqlrest.common.enums.DataTypeFormatEnum;
import org.dromara.sqlrest.common.enums.ExecuteEngineEnum;
import org.dromara.sqlrest.common.enums.HttpMethodEnum;
import org.dromara.sqlrest.common.enums.NamingStrategyEnum;

@Data
public class ExpImpAssignmentModel {

  private String groupName;

  private String moduleName;

  private ExpImpDataSourceModel dataSourceModel;

  private String name;

  private String description;

  private HttpMethodEnum method;

  private String path;

  private List<ItemParam> params;

  private List<OutParam> outputs;

  private Boolean open;

  private Boolean alarm;

  private String contentType;

  private ExecuteEngineEnum engine;

  private Map<DataTypeFormatEnum, String> responseFormat;

  private NamingStrategyEnum namingStrategy;

  private Boolean flowStatus;

  private Integer flowGrade;

  private Integer flowCount;

  private CacheKeyTypeEnum cacheKeyType;

  private String cacheKeyExpr;

  private Long cacheExpireSeconds;

  private List<String> contextList;
}
