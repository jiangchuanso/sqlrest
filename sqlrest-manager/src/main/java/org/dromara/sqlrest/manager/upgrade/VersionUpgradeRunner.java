// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.manager.upgrade;

import cn.hutool.extra.spring.SpringUtil;
import java.util.List;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.sqlrest.persistence.dao.ApiAssignmentDao;
import org.dromara.sqlrest.persistence.dao.ApiOnlineDao;
import org.dromara.sqlrest.persistence.dao.VersionCommitDao;
import org.dromara.sqlrest.persistence.entity.ApiAssignmentEntity;
import org.dromara.sqlrest.persistence.entity.ApiOnlineEntity;
import org.dromara.sqlrest.persistence.entity.VersionCommitEntity;
import org.dromara.sqlrest.persistence.util.JsonUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

/**
 * 自动升级VCS数据
 *
 * @author suntang
 */
@Slf4j
@Order(2)
@Component
@RequiredArgsConstructor
public class VersionUpgradeRunner implements ApplicationRunner {

  private static final String COMMIT_INIT_DESC = "First Commit By Upgrade.";

  private final ApiAssignmentDao apiAssignmentDao;
  private final VersionCommitDao versionCommitDao;
  private final ApiOnlineDao apiOnlineDao;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    VersionUpgradeRunner runner = SpringUtil.getBean(VersionUpgradeRunner.class);
    log.info("Startup upgrade VCS data now.");
    StopWatch stopWatch = new StopWatch();
    stopWatch.start("UpgradeVersion");
    List<Long> idList = apiAssignmentDao.getUpgradeOnlineAssignments();
    if (CollectionUtils.isNotEmpty(idList)) {
      for (Long id : idList) {
        if (null == versionCommitDao.getLatestVersion(id)) {
          runner.upgradeApiAssignment(apiAssignmentDao.getById(id, true));
        }
      }
      Lists.partition(idList, 500).forEach(apiAssignmentDao::resetUpgradeOnlineAssignments);
    }
    stopWatch.stop();
    log.info("Success upgrade version control for {} APIs, total cost {} ms .",
        idList.size(), stopWatch.getTotalTimeMillis());
  }

  @Transactional(rollbackFor = Exception.class)
  public void upgradeApiAssignment(ApiAssignmentEntity assignment) {
    String content = JsonUtils.toJsonString(assignment);
    Long bizId = assignment.getId();
    VersionCommitEntity commitEntity = versionCommitDao.createVersion(bizId, COMMIT_INIT_DESC, content);
    ApiOnlineEntity onlineEntity = ApiOnlineEntity.builder()
        .name(assignment.getName())
        .method(assignment.getMethod())
        .path(assignment.getPath())
        .apiId(assignment.getId())
        .groupId(assignment.getGroupId())
        .moduleId(assignment.getModuleId())
        .datasourceId(assignment.getDatasourceId())
        .open(assignment.getOpen())
        .alarm(assignment.getAlarm())
        .flowStatus(assignment.getFlowStatus())
        .commitId(commitEntity.getId())
        .version(commitEntity.getVersion())
        .content(content)
        .build();
    apiOnlineDao.upsert(onlineEntity);
  }
}
