// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.exec.module;

import cn.hutool.extra.spring.SpringUtil;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.util.Map;
import org.dromara.sqlrest.common.enums.NamingStrategyEnum;
import org.dromara.sqlrest.common.enums.ProductTypeEnum;
import org.dromara.sqlrest.core.driver.DriverLoadService;
import org.dromara.sqlrest.core.exec.annotation.Comment;
import org.dromara.sqlrest.core.exec.annotation.Module;
import org.dromara.sqlrest.core.util.DataSourceUtils;
import org.dromara.sqlrest.persistence.dao.DataSourceDao;
import org.dromara.sqlrest.persistence.entity.DataSourceEntity;

@Module("ds")
public class DsVarModule {

  private DataSourceDao dataSourceDao = SpringUtil.getBean(DataSourceDao.class);
  private DriverLoadService driverLoadService = SpringUtil.getBean(DriverLoadService.class);

  private Map<String, Object> params;
  private ProductTypeEnum productType;
  private NamingStrategyEnum strategy;

  public DsVarModule(ProductTypeEnum productType, Map<String, Object> params, NamingStrategyEnum strategy) {
    this.productType = productType;
    this.params = params;
    this.strategy = strategy;
  }

  @Comment("根据数据源ID获取db模块")
  public DbVarModule getDB(@Comment("id") Long id) {
    DataSourceEntity dsEntity = dataSourceDao.getById(id);
    if (null == dsEntity) {
      throw new RuntimeException("Not found id=" + id + " data source!");
    }
    File driverPath = driverLoadService.getVersionDriverFile(dsEntity.getType(), dsEntity.getVersion());
    HikariDataSource dataSource = DataSourceUtils.getHikariDataSource(dsEntity, driverPath.getAbsolutePath());
    return new DbVarModule(dataSource, productType, params, strategy);
  }
}

