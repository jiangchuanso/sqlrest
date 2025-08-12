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

import org.dromara.sqlrest.common.enums.ParamTypeEnum;
import org.dromara.sqlrest.common.exception.CommonException;
import org.dromara.sqlrest.common.exception.ResponseErrorCode;
import org.dromara.sqlrest.persistence.dao.SystemParamDao;
import org.dromara.sqlrest.persistence.entity.SystemParamEntity;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SystemParamService {

  @Resource
  private SystemParamDao systemParamDao;

  public Object getByParamKey(String key) {
    SystemParamEntity entity = systemParamDao.getByParamKey(key);
    if (null == entity) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_NOT_EXISTS, "NO KEY:" + key);
    }
    Class clazz = entity.getParamType().getClazz();
    String paramValue = entity.getParamValue();
    return clazz.cast(entity.getParamType().getConverter().apply(paramValue));
  }

  public void updateByParamKey(String key, String value) {
    SystemParamEntity entity = systemParamDao.getByParamKey(key);
    if (null == entity) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_NOT_EXISTS, "NO KEY:" + key);
    }
    Class clazz = entity.getParamType().getClazz();
    Object paramValue = clazz.cast(entity.getParamType().getConverter().apply(value));
    if (null == paramValue) {
      throw new CommonException(ResponseErrorCode.ERROR_INTERNAL_ERROR, "Invalid param value:[" + value + "]");
    }
    systemParamDao.updateByParamKey(key, String.valueOf(paramValue));
  }

  public Integer getIntByParamKey(String key, int defaultValue) {
    SystemParamEntity entity = systemParamDao.getByParamKey(key);
    if (null == entity) {
      return defaultValue;
    }
    if (ParamTypeEnum.LONG.equals(entity.getParamType())) {
      try {
        return Integer.parseInt(entity.getParamValue());
      } catch (Exception e) {
        log.warn("Read system param integer value by key={} failed,use default value={},error:{} ",
            key, defaultValue, e.getMessage());
      }
    }
    return defaultValue;
  }
}
