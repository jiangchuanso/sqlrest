// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.sqlrest.core.service;

import com.gitee.sqlrest.common.dto.PageResult;
import com.gitee.sqlrest.common.exception.CommonException;
import com.gitee.sqlrest.common.exception.ResponseErrorCode;
import com.gitee.sqlrest.core.dto.EntitySearchRequest;
import com.gitee.sqlrest.persistence.dao.ApiAssignmentDao;
import com.gitee.sqlrest.persistence.dao.ApiGroupDao;
import com.gitee.sqlrest.persistence.dao.AppClientDao;
import com.gitee.sqlrest.persistence.entity.ApiGroupEntity;
import com.gitee.sqlrest.persistence.util.PageUtils;
import javax.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApiGroupService {

  @Resource
  private ApiGroupDao apiGroupDao;
  @Resource
  private ApiAssignmentDao apiAssignmentDao;
  @Resource
  private AppClientDao appClientDao;

  public void createGroup(String name) {
    try {
      apiGroupDao.insert(ApiGroupEntity.builder().name(name).build());
    } catch (DuplicateKeyException e) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_ALREADY_EXISTS, "group name already exists");
    }
  }

  public void updateGroup(Long id, String newName) {
    ApiGroupEntity apiGroupEntity = apiGroupDao.getById(id);
    apiGroupEntity.setName(newName);
    try {
      apiGroupDao.updateById(apiGroupEntity);
    } catch (DuplicateKeyException e) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_ALREADY_EXISTS, "group name already exists");
    }
  }

  @Transactional(rollbackFor = Exception.class)
  public void deleteGroup(Long id) {
    if (apiAssignmentDao.existsGroupById(id)) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_ALREADY_USED, "used by api config");
    }
    apiGroupDao.deleteById(id);
    appClientDao.deleteClientAuthByGroupId(id);
  }

  public PageResult<ApiGroupEntity> listAll(EntitySearchRequest request) {
    return PageUtils.getPage(
        () -> apiGroupDao.listAll(request.getSearchText()),
        request.getPage(),
        request.getSize()
    );
  }
}
