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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.dromara.sqlrest.common.dto.DateCount;
import org.dromara.sqlrest.common.dto.HourCount;
import org.dromara.sqlrest.common.dto.NameCount;
import org.dromara.sqlrest.common.dto.PageResult;
import org.dromara.sqlrest.core.dto.ApiAccessLogBasicResponse;
import org.dromara.sqlrest.persistence.dao.AppClientDao;
import org.dromara.sqlrest.persistence.entity.AccessRecordEntity;
import org.dromara.sqlrest.persistence.entity.AppClientEntity;
import org.dromara.sqlrest.persistence.mapper.AccessRecordMapper;
import org.dromara.sqlrest.persistence.mapper.ApiAssignmentMapper;
import org.dromara.sqlrest.persistence.mapper.DataSourceMapper;
import org.dromara.sqlrest.persistence.util.PageUtils;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OverviewService {

  private final AccessRecordMapper accessRecordMapper;
  private final DataSourceMapper dataSourceMapper;
  private final ApiAssignmentMapper apiAssignmentMapper;

  public Map<String, Integer> count() {
    return accessRecordMapper.selectCount();
  }

  public List<DateCount> trend(Integer days) {
    return accessRecordMapper.getDailyTrend(days > 0 ? days - 1 : days);
  }

  public List<NameCount> httpStatus(Integer days) {
    return accessRecordMapper.getHttpStatusCount(days > 0 ? days - 1 : days);
  }

  public List<NameCount> topPath(Integer days, Integer n) {
    return accessRecordMapper.getTopPathAccess(days > 0 ? days - 1 : days, n);
  }

  public List<NameCount> topAddr(Integer days, Integer n) {
    return accessRecordMapper.getTopIpAddrAccess(days > 0 ? days - 1 : days, n);
  }

  public List<NameCount> topClient(Integer days, Integer n) {
    return accessRecordMapper.getTopAppClientAccess(days > 0 ? days - 1 : days, n);
  }

  public List<NameCount> datasourceTypeRatio() {
    return dataSourceMapper.getDatasourceTypeRatio();
  }

  public List<NameCount> engineRatio() {
    return apiAssignmentMapper.getEngineRatio();
  }

  public List<NameCount> datasourceApiCount() {
    return apiAssignmentMapper.getDatasourceApiCount();
  }

  public List<NameCount> methodRatio() {
    return apiAssignmentMapper.getMethodRatio();
  }

  public List<NameCount> moduleApiCount() {
    return apiAssignmentMapper.getModuleApiCount(15);
  }

  public List<NameCount> apiStatusRatio(Long apiId, Integer days) {
    return accessRecordMapper.getApiHttpStatusCount(apiId, days > 0 ? days - 1 : days);
  }

  public List<DateCount> apiDailyTrend(Long apiId, Integer days) {
    return accessRecordMapper.getApiDailyTrend(apiId, days > 0 ? days - 1 : days);
  }

  public List<HourCount> apiHourlyTrend(Long apiId, String date) {
    return accessRecordMapper.getApiHourlyTrend(apiId, date);
  }

  public PageResult<ApiAccessLogBasicResponse> pageByApiId(Long apiId, Integer page, Integer size,
      Integer statusCode, String startTime, String endTime) {
    Map<String, String> map = SpringUtil.getBean(AppClientDao.class)
        .listAll().stream()
        .collect(
            Collectors.toMap(
                AppClientEntity::getAppKey,
                one -> String.format("[%d]%s(%s)", one.getId(), one.getName(), one.getAppKey())));
    LambdaQueryWrapper<AccessRecordEntity> query = Wrappers.<AccessRecordEntity>lambdaQuery()
        .eq(AccessRecordEntity::getApiId, apiId);
    if (statusCode != null) {
      query.eq(AccessRecordEntity::getStatus, statusCode);
    }
    if (startTime != null && !startTime.isEmpty()) {
      query.ge(AccessRecordEntity::getCreateTime, startTime);
    }
    if (endTime != null && !endTime.isEmpty()) {
      query.le(AccessRecordEntity::getCreateTime, endTime);
    }
    query.orderByDesc(AccessRecordEntity::getId);
    return PageUtils.getPage(() ->
            accessRecordMapper.selectList(query).stream().map(
                record -> ApiAccessLogBasicResponse.builder()
                    .id(record.getId())
                    .status(record.getStatus())
                    .duration(record.getDuration())
                    .ipAddr(record.getIpAddr())
                    .userAgent(record.getUserAgent())
                    .clientApp(map.get(record.getClientKey()))
                    .parameters(record.getParameters())
                    .exception(record.getException())
                    .createTime(record.getCreateTime())
                    .executorAddr(record.getExecutorAddr())
                    .gatewayAddr(record.getGatewayAddr())
                    .build()
            ).collect(Collectors.toList())
        , page, size);
  }
}
