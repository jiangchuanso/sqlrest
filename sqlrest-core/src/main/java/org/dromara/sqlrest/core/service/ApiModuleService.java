// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.service;

import cn.hutool.extra.spring.SpringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.dromara.sqlrest.common.dto.PageResult;
import org.dromara.sqlrest.common.exception.CommonException;
import org.dromara.sqlrest.common.exception.ResponseErrorCode;
import org.dromara.sqlrest.core.dto.ApiModuleAssignments;
import org.dromara.sqlrest.core.dto.EntitySearchRequest;
import org.dromara.sqlrest.core.dto.SelectedEntityIdName;
import org.dromara.sqlrest.persistence.dao.ApiAssignmentDao;
import org.dromara.sqlrest.persistence.dao.ApiModuleDao;
import org.dromara.sqlrest.persistence.dao.ApiOnlineDao;
import org.dromara.sqlrest.persistence.entity.ApiModuleEntity;
import org.dromara.sqlrest.persistence.entity.ModuleAssignmentEntity;
import org.dromara.sqlrest.persistence.util.PageUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApiModuleService {

  @Resource
  private ApiModuleDao apiModuleDao;
  @Resource
  private ApiAssignmentDao apiAssignmentDao;

  public void createModule(String name) {
    try {
      apiModuleDao.insert(ApiModuleEntity.builder().name(name).build());
    } catch (DuplicateKeyException e) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_ALREADY_EXISTS, "module name already exists");
    }
  }

  public void updateModule(Long id, String newName) {
    ApiModuleEntity moduleEntity = apiModuleDao.getById(id);
    moduleEntity.setName(newName);
    try {
      apiModuleDao.updateById(moduleEntity);
    } catch (DuplicateKeyException e) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_ALREADY_EXISTS, "module name already exists");
    }
  }

  @Transactional(rollbackFor = Exception.class)
  public void deleteModule(Long id) {
    if (apiAssignmentDao.existsModuleById(id)) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_ALREADY_USED, "used by api config");
    }
    if (SpringUtil.getBean(ApiOnlineDao.class).existsModuleById(id)) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_ALREADY_USED, "used by api config");
    }
    apiModuleDao.deleteById(id);
  }

  public PageResult<ApiModuleEntity> listAll(EntitySearchRequest request) {
    return PageUtils.getPage(
        () -> apiModuleDao.listAll(request.getSearchText()),
        request.getPage(),
        request.getSize()
    );
  }

  public List<ApiModuleAssignments> moduleTree(Long groupId) {
    Map<Long, List<ModuleAssignmentEntity>> moduleIdListMap = apiAssignmentDao.getModuleAssignments()
        .stream().collect(Collectors.groupingBy(ModuleAssignmentEntity::getModuleId));
    List<ApiModuleAssignments> results = new ArrayList<>(moduleIdListMap.size());
    for (Map.Entry<Long, List<ModuleAssignmentEntity>> entry : moduleIdListMap.entrySet()) {
      ModuleAssignmentEntity first = entry.getValue().get(0);
      ApiModuleAssignments module = new ApiModuleAssignments();
      module.setId(first.getModuleId());
      module.setName(first.getModuleName());
      module.setChildren(
          entry.getValue().stream()
              .map(one ->
                  SelectedEntityIdName.builder()
                      .id(one.getAssigmentId())
                      .name(String.format("[%d]%s", one.getAssigmentId(), one.getAssigmentName()))
                      .selected(one.getGroupId().equals(groupId))
                      .build())
              .collect(Collectors.toList()));
      results.add(module);
    }
    return results;
  }
}
