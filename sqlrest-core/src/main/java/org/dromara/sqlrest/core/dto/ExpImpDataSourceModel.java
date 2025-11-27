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

import lombok.Data;
import org.dromara.sqlrest.common.enums.ProductTypeEnum;

@Data
public class ExpImpDataSourceModel {

  private String name;

  private ProductTypeEnum type;

  private String version;

  private String driver;

  private String url;

  private String username;

  private String password;
}
