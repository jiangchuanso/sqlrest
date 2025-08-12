// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.manager.service;

import org.dromara.sqlrest.persistence.mapper.AccessRecordMapper;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AccessLogCleanService {

  @Value("${access.log.clean.days:30}")
  private Integer cleanJobLogDays;

  @Resource
  private AccessRecordMapper accessRecordMapper;

  @EventListener(ApplicationReadyEvent.class)
  public void cleanOnceAfterRestart() {
    doCleanHistoryLog();
  }

  @Scheduled(cron = "0 0 0 * * ? ")
  public void cleanSchedule() {
    doCleanHistoryLog();
  }

  private void doCleanHistoryLog() {
    try {
      accessRecordMapper.deleteHistoryBeforeDays(cleanJobLogDays);
      log.info("Success to clean history access log for {} days", cleanJobLogDays);
    } catch (Throwable t) {
      log.error("Failed to clean history access log,", t);
    }
  }
}
