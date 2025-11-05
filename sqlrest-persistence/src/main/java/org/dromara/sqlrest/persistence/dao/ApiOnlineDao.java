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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.dromara.sqlrest.common.dto.ApiIdVersion;
import org.dromara.sqlrest.common.enums.HttpMethodEnum;
import org.dromara.sqlrest.persistence.entity.ApiAssignmentEntity;
import org.dromara.sqlrest.persistence.entity.ApiOnlineEntity;
import org.dromara.sqlrest.persistence.mapper.ApiOnlineMapper;
import org.dromara.sqlrest.persistence.util.JsonUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Repository
public class ApiOnlineDao {

  @Resource
  private ApiOnlineMapper apiOnlineMapper;

  public void upsert(ApiOnlineEntity entity) {
    Long id = getIdByUniqueKey(entity.getMethod(), entity.getPath());
    if (null == id) {
      apiOnlineMapper.insert(entity);
    } else {
      entity.setId(id);
      apiOnlineMapper.updateById(entity);
    }
  }

  public void deleteByApiId(Long apiId) {
    QueryWrapper<ApiOnlineEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda().eq(ApiOnlineEntity::getApiId, apiId);
    apiOnlineMapper.delete(queryWrapper);
  }

  public ApiAssignmentEntity getByApiId(Long apiId) {
    QueryWrapper<ApiOnlineEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda()
        .select(ApiOnlineEntity::getContent)
        .eq(ApiOnlineEntity::getApiId, apiId);
    ApiOnlineEntity entity = apiOnlineMapper.selectOne(queryWrapper);
    if (null != entity) {
      return JsonUtils.toBeanObject(entity.getContent(), ApiAssignmentEntity.class);
    }
    return null;
  }

  public List<ApiIdVersion> filterOnline(List<Long> apiIds) {
    if (CollectionUtils.isEmpty(apiIds)) {
      return Collections.emptyList();
    }
    return apiOnlineMapper.filterOnline(apiIds);
  }

  public ApiIdVersion filterOnline(Long apiId) {
    return filterOnline(Collections.singletonList(apiId))
        .stream().findFirst().orElse(null);
  }

  public List<ApiAssignmentEntity> listAll() {
    return apiOnlineMapper.selectList(null)
        .stream().map(entity -> JsonUtils.toBeanObject(entity.getContent(), ApiAssignmentEntity.class))
        .collect(Collectors.toList());
  }

  public List<ApiAssignmentEntity> searchAll(Long groupId, Long moduleId, Boolean open, String searchText) {
    return apiOnlineMapper.selectList(
        Wrappers.<ApiOnlineEntity>lambdaQuery()
            .select(ApiOnlineEntity::getContent)
            .eq(Objects.nonNull(open), ApiOnlineEntity::getOpen, open)
            .eq(Objects.nonNull(groupId), ApiOnlineEntity::getGroupId, groupId)
            .eq(Objects.nonNull(moduleId), ApiOnlineEntity::getModuleId, moduleId)
            .like(StringUtils.hasText(searchText), ApiOnlineEntity::getName, searchText)
            .orderByDesc(ApiOnlineEntity::getCreateTime)
    ).stream().map(entity -> JsonUtils.toBeanObject(entity.getContent(), ApiAssignmentEntity.class))
        .collect(Collectors.toList());
  }

  public ApiAssignmentEntity getByUk(HttpMethodEnum method, String path) {
    QueryWrapper<ApiOnlineEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda()
        .select(ApiOnlineEntity::getContent)
        .eq(ApiOnlineEntity::getMethod, method)
        .eq(ApiOnlineEntity::getPath, path);
    ApiOnlineEntity entity = apiOnlineMapper.selectOne(queryWrapper);
    if (null != entity) {
      return JsonUtils.toBeanObject(entity.getContent(), ApiAssignmentEntity.class);
    }
    return null;
  }

  public Long getCommitIdByUk(HttpMethodEnum method, String path) {
    QueryWrapper<ApiOnlineEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda()
        .select(ApiOnlineEntity::getCommitId)
        .eq(ApiOnlineEntity::getMethod, method)
        .eq(ApiOnlineEntity::getPath, path);
    ApiOnlineEntity entity = apiOnlineMapper.selectOne(queryWrapper);
    if (null != entity) {
      return entity.getCommitId();
    }
    return null;
  }

  public boolean existsByUniqueKey(HttpMethodEnum method, String path) {
    return null != getIdByUniqueKey(method, path);
  }

  private Long getIdByUniqueKey(HttpMethodEnum method, String path) {
    QueryWrapper<ApiOnlineEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda()
        .select(ApiOnlineEntity::getId)
        .eq(ApiOnlineEntity::getMethod, method)
        .eq(ApiOnlineEntity::getPath, path);
    ApiOnlineEntity entity = apiOnlineMapper.selectOne(queryWrapper);
    if (null != entity) {
      return entity.getId();
    }
    return null;
  }

  public List<ApiAssignmentEntity> listFlowControlAll() {
    QueryWrapper<ApiOnlineEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda()
        .select(ApiOnlineEntity::getContent)
        .eq(ApiOnlineEntity::getFlowStatus, true);
    return apiOnlineMapper.selectList(queryWrapper)
        .stream().map(entity -> JsonUtils.toBeanObject(entity.getContent(), ApiAssignmentEntity.class))
        .collect(Collectors.toList());
  }

  public boolean existsDataSourceById(Long dataSourceId) {
    QueryWrapper<ApiOnlineEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda().eq(ApiOnlineEntity::getDatasourceId, dataSourceId);
    return apiOnlineMapper.selectCount(queryWrapper) > 0;
  }

  public boolean existsGroupById(Long groupId) {
    QueryWrapper<ApiOnlineEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda().eq(ApiOnlineEntity::getGroupId, groupId);
    return apiOnlineMapper.selectCount(queryWrapper) > 0;
  }

  public boolean existsModuleById(Long moduleId) {
    QueryWrapper<ApiOnlineEntity> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda().eq(ApiOnlineEntity::getModuleId, moduleId);
    return apiOnlineMapper.selectCount(queryWrapper) > 0;
  }
}
