// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.persistence.dao;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Resource;
import org.dromara.sqlrest.persistence.entity.VersionCommitEntity;
import org.dromara.sqlrest.persistence.mapper.VersionCommitMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class VersionCommitDao {

  @Resource
  private VersionCommitMapper versionCommitMapper;

  @Transactional(rollbackFor = Exception.class)
  public VersionCommitEntity createVersion(Long bizId, String description, String content) {
    Integer currMaxVersion = versionCommitMapper.getMaxVersion(bizId);
    VersionCommitEntity entity = VersionCommitEntity.builder()
        .bizId(bizId)
        .version(Optional.ofNullable(currMaxVersion).orElse(0) + 1)
        .description(description)
        .content(content)
        .build();
    versionCommitMapper.insert(entity);
    return entity;
  }

  public VersionCommitEntity getLatestVersion(Long bizId) {
    return versionCommitMapper.getLatestVersion(bizId);
  }

  public VersionCommitEntity getByCommitId(Long commitId) {
    return versionCommitMapper.selectById(commitId);
  }

  public List<VersionCommitEntity> getVersionList(Long bizId, boolean withContent) {
    List<SFunction<VersionCommitEntity, ?>> columns = new ArrayList<>();
    columns.add(VersionCommitEntity::getId);
    columns.add(VersionCommitEntity::getBizId);
    columns.add(VersionCommitEntity::getVersion);
    columns.add(VersionCommitEntity::getDescription);
    columns.add(VersionCommitEntity::getCreateTime);
    if (withContent) {
      columns.add(VersionCommitEntity::getContent);
    }
    return versionCommitMapper.selectList(
        Wrappers.<VersionCommitEntity>lambdaQuery()
            .select(columns)
            .eq(VersionCommitEntity::getBizId, bizId)
            .orderByDesc(VersionCommitEntity::getVersion)
    );
  }
}
