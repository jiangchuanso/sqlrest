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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import org.dromara.sqlrest.cache.CacheFactory;
import org.dromara.sqlrest.common.consts.Constants;
import org.dromara.sqlrest.common.dto.AccessToken;
import org.dromara.sqlrest.common.dto.PageResult;
import org.dromara.sqlrest.common.enums.DurationTimeEnum;
import org.dromara.sqlrest.common.enums.ExpireTimeEnum;
import org.dromara.sqlrest.common.exception.CommonException;
import org.dromara.sqlrest.common.exception.ResponseErrorCode;
import org.dromara.sqlrest.common.util.TokenUtils;
import org.dromara.sqlrest.core.dto.AppClientDetailResponse;
import org.dromara.sqlrest.core.dto.AppClientGroupRequest;
import org.dromara.sqlrest.core.dto.AppClientSaveRequest;
import org.dromara.sqlrest.core.dto.AppClientSearchRequest;
import org.dromara.sqlrest.core.dto.EntityIdNameResponse;
import org.dromara.sqlrest.persistence.dao.AppClientDao;
import org.dromara.sqlrest.persistence.entity.AppClientEntity;
import org.dromara.sqlrest.persistence.util.PageUtils;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class AppClientService {

  @Resource
  private AppClientDao appClientDao;
  @Resource
  private CacheFactory cacheFactory;

  public void create(AppClientSaveRequest request) {
    if (null != appClientDao.getByAppKey(request.getAppKey())) {
      throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT, "Duplicate app key :" + request.getAppKey());
    }
    if (appClientDao.getByName(request.getName()).size() > 0) {
      throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT, "Duplicate Name :" + request.getName());
    }

    AppClientEntity appClientEntity = new AppClientEntity();
    BeanUtil.copyProperties(request, appClientEntity);
    appClientEntity.setAppSecret(TokenUtils.generateValue());
    appClientEntity.setExpireDuration(request.getExpireTime().getDuration());
    if (DurationTimeEnum.TIME_VALUE.equals(request.getExpireTime().getDuration())) {
      appClientEntity.setExpireAt(request.getExpireTime().getValue() + (System.currentTimeMillis() / 1000));
    } else if (DurationTimeEnum.FOR_EVER.equals(request.getExpireTime().getDuration())) {
      appClientEntity.setExpireAt(-1L);
    } else {
      appClientEntity.setExpireAt(0L);
    }
    appClientEntity.setAccessToken(null);
    appClientEntity.setTokenAlive(request.getTokenAlive());
    appClientDao.insert(appClientEntity);
  }

  public void delete(Long id) {
    AppClientEntity entity = appClientDao.getById(id);
    if (null != entity) {
      String token = entity.getAccessToken();
      if (StringUtils.isNotBlank(token)) {
        Map<String, AccessToken> tokenClientMap = cacheFactory
            .getCacheMap(Constants.CACHE_KEY_TOKEN_CLIENT, AccessToken.class);
        tokenClientMap.remove(token);
      }
      appClientDao.deleteById(id);
    }
  }

  public PageResult<AppClientDetailResponse> searchList(AppClientSearchRequest request) {
    Supplier<List<AppClientDetailResponse>> method = () -> {
      List<AppClientEntity> list = appClientDao.listAll(request.getSearchText(), request.getGroupId());
      return list.stream().map(appClientEntity -> {
        AppClientDetailResponse response = new AppClientDetailResponse();
        BeanUtil.copyProperties(appClientEntity, response);
        ExpireTimeEnum expireTime = ExpireTimeEnum
            .from(appClientEntity.getExpireDuration(), appClientEntity.getExpireAt());
        Boolean isExpired = (System.currentTimeMillis() / 1000) > response.getExpireAt();
        if (DurationTimeEnum.TIME_VALUE != appClientEntity.getExpireDuration()) {
          isExpired = false;
        }
        response.setExpireType(expireTime.getDescription());
        response.setIsExpired(isExpired);
        if (response.getExpireAt() > 0) {
          long expireAt = response.getExpireAt();
          response.setExpireAtStr(DateUtil.format(new Date(expireAt * 1000), "yyyy-MM-dd HH:mm:ss"));
        }
        return response;
      }).collect(Collectors.toList());
    };

    return PageUtils.getPage(method, request.getPage(), request.getSize());
  }

  public String getSecret(Long id) {
    AppClientEntity entity = appClientDao.getById(id);
    if (null == entity) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_NOT_EXISTS, "id=" + id);
    }
    return entity.getAppSecret();
  }

  public void createGroupAuth(AppClientGroupRequest request) {
    appClientDao.saveAuthGroup(request.getId(), request.getGroupIds());
  }

  public List<EntityIdNameResponse> getGroupAuth(Long id) {
    return appClientDao.getGroupAuth(id).stream()
        .map(item -> new EntityIdNameResponse(item.getId(), item.getName()))
        .collect(Collectors.toList());
  }

}
