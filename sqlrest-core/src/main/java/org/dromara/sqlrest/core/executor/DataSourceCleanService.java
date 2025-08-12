// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.executor;

import org.dromara.sqlrest.core.util.DataSourceUtils;
import org.dromara.sqlrest.persistence.dao.DataSourceDao;
import java.util.Set;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DataSourceCleanService {

  @Resource
  private DataSourceDao dataSourceDao;

  /*每小时执行一次*/
  @EventListener(ApplicationReadyEvent.class)
  @Scheduled(cron = "${cron.datasource.clean.expression:0 0 0/1 * * ? }")
  public void autoClean() {
    log.info("Start check deleted datasource for close it ...");
    try {
      Set<Long> running = DataSourceUtils.getAllDataSourceIdSet();
      Set<Long> exists = dataSourceDao.getAllIdList();
      for (Long id : running) {
        if (!exists.contains(id)) {
          DataSourceUtils.dropHikariDataSource(id);
        }
      }
    } catch (Exception e) {
      log.warn("Failed to auto clean deleted datasource,error: {}", e.getMessage(), e);
    }
  }


}
