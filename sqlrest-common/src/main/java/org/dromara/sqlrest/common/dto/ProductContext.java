// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.common.dto;

import java.io.Serializable;
import java.sql.Connection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductContext implements Serializable {

  private int id;
  private String quote;
  private String name;
  private String driver;
  private int defaultPort;
  private boolean multiDialect;
  private String testSql;
  private String urlPrefix;
  private String[] tplUrls;
  private String urlSample;
  private String sqlSchemaList;
  private List<String> retSchemaList;
  private boolean hasCatalogAndSchema;
  private Function<String, Pair<String, String>> adapter;
  private String pageSql;
  private ThreeConsumer<Integer, Integer, List<Object>> pageConsumer;
  private Consumer<Connection> executeBeforeQuery;
}
