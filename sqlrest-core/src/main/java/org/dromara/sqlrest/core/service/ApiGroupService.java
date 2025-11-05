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
import javax.annotation.Resource;
import org.dromara.sqlrest.common.dto.PageResult;
import org.dromara.sqlrest.common.exception.CommonException;
import org.dromara.sqlrest.common.exception.ResponseErrorCode;
import org.dromara.sqlrest.core.dto.EntitySearchRequest;
import org.dromara.sqlrest.persistence.dao.ApiAssignmentDao;
import org.dromara.sqlrest.persistence.dao.ApiGroupDao;
import org.dromara.sqlrest.persistence.dao.ApiOnlineDao;
import org.dromara.sqlrest.persistence.dao.AppClientDao;
import org.dromara.sqlrest.persistence.entity.ApiGroupEntity;
import org.dromara.sqlrest.persistence.util.PageUtils;
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
    if (id.equals(1L)) {
      throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT, "forbid delete group which id is 1");
    }
    if (apiAssignmentDao.existsGroupById(id)) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_ALREADY_USED, "used by api config");
    }
    if (SpringUtil.getBean(ApiOnlineDao.class).existsGroupById(id)) {
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
