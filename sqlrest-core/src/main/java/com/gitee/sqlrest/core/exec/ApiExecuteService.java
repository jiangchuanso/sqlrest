// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.sqlrest.core.exec;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.gitee.sqlrest.cache.CacheFactory;
import com.gitee.sqlrest.cache.DistributedCache;
import com.gitee.sqlrest.common.consts.Constants;
import com.gitee.sqlrest.common.dto.ItemParam;
import com.gitee.sqlrest.common.dto.ResultEntity;
import com.gitee.sqlrest.common.enums.CacheKeyTypeEnum;
import com.gitee.sqlrest.common.enums.HttpMethodEnum;
import com.gitee.sqlrest.common.enums.ParamLocationEnum;
import com.gitee.sqlrest.common.enums.ParamTypeEnum;
import com.gitee.sqlrest.common.exception.CommonException;
import com.gitee.sqlrest.common.exception.ResponseErrorCode;
import com.gitee.sqlrest.core.driver.DriverLoadService;
import com.gitee.sqlrest.core.exec.engine.ApiExecutorEngineFactory;
import com.gitee.sqlrest.core.exec.extractor.HttpRequestBodyExtractor;
import com.gitee.sqlrest.core.util.DataSourceUtils;
import com.gitee.sqlrest.core.util.SpelUtils;
import com.gitee.sqlrest.persistence.dao.DataSourceDao;
import com.gitee.sqlrest.persistence.entity.ApiAssignmentEntity;
import com.gitee.sqlrest.persistence.entity.DataSourceEntity;
import com.gitee.sqlrest.persistence.util.JsonUtils;
import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApiExecuteService {

  @Resource
  private CacheFactory cacheFactory;
  @Resource
  private DataSourceDao dataSourceDao;
  @Resource
  private DriverLoadService driverLoadService;
  @Resource
  private List<HttpRequestBodyExtractor> requestBodyExtractors;

  private DistributedCache getDistributedCache() {
    return cacheFactory.getDistributedCache(Constants.CACHE_NAME_API_RESPONSE);
  }

  private String getCacheKeyValue(ApiAssignmentEntity config, Map<String, Object> paramValues) {
    String key = CacheKeyTypeEnum.SPEL == config.getCacheKeyType()
        ? SpelUtils.getExpressionValue(config.getCacheKeyExpr(), paramValues)
        : DigestUtil.sha256Hex(JsonUtils.toJsonString(paramValues));
    return Constants.getResourceName(config.getMethod().name(), config.getPath()) + ":" + key;
  }

  public ResultEntity<Object> execute(ApiAssignmentEntity config, HttpServletRequest request) {
    String resourceName = Constants.getResourceName(config.getMethod().name(), config.getPath());
    try {
      List<ItemParam> invalidArgs = new ArrayList<>();
      Map<String, Object> paramValues = mergeParameters(request, config.getParams(), invalidArgs);
      if (invalidArgs.size() > 0) {
        throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT, convertInvalidArgs(invalidArgs));
      }
      return execute(config, paramValues);
    } catch (CommonException e) {
      log.warn("Failed to valid parameters for {}, error:{}", resourceName, e.getMessage());
      return ResultEntity.failed(e.getCode(), e.getMessage());
    } catch (Throwable t) {
      log.warn("Failed to execute for {}, error:{}", resourceName, t.getMessage());
      return ResultEntity.failed(ResponseErrorCode.ERROR_INTERNAL_ERROR, ExceptionUtil.getMessage(t));
    }
  }

  public ResultEntity<Object> execute(ApiAssignmentEntity config, Map<String, Object> paramValues) {
    if (config.getCacheKeyType().isUseCache()) {
      String key = getCacheKeyValue(config, paramValues);
      DistributedCache cache = getDistributedCache();
      ResultEntity result = cache.get(key, ResultEntity.class);
      if (null == result) {
        result = doExecute(getDataSourceEntity(config), config, paramValues);
        cache.put(key, result, config.getCacheExpireSeconds(), TimeUnit.SECONDS);
      } else {
        String resourceName = Constants.getResourceName(config.getMethod().name(), config.getPath());
        log.info("Execute for {} find cache response by cacheKey={}", resourceName, key);
      }
      return result;
    } else {
      return doExecute(getDataSourceEntity(config), config, paramValues);
    }
  }

  private DataSourceEntity getDataSourceEntity(ApiAssignmentEntity config) {
    DataSourceEntity dsEntity = dataSourceDao.getById(config.getDatasourceId());
    if (null == dsEntity) {
      String message = "datasource[id=" + config.getDatasourceId() + " not exist!";
      log.warn("Error for handle api[id={}],information:{}", config.getId(), message);
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_NOT_EXISTS, message);
    }
    return dsEntity;
  }

  private ResultEntity doExecute(DataSourceEntity dsEntity, ApiAssignmentEntity config,
      Map<String, Object> paramValues) {
    File driverPath = driverLoadService.getVersionDriverFile(dsEntity.getType(), dsEntity.getVersion());
    HikariDataSource dataSource = DataSourceUtils.getHikariDataSource(dsEntity, driverPath.getAbsolutePath());
    List<Object> results = ApiExecutorEngineFactory
        .getExecutor(config.getEngine(), dataSource, dsEntity.getType())
        .execute(config.getContextList(), paramValues, config.getNamingStrategy());
    return ResultEntity.success(results.size() > 1 ? results : results.stream().findAny().orElse(null));
  }

  private String convertInvalidArgs(List<ItemParam> invalidArgs) {
    return "无效参数," + invalidArgs.stream().map(
        p -> (p.getIsArray() ? "数组" : "") + "参数'" + p.getName() + "'位于" + p.getLocation().getIn()
    ).collect(Collectors.joining(";"));
  }

  private Map<String, Object> mergeParameters(HttpServletRequest request, List<ItemParam> params,
      List<ItemParam> invalidArgs) throws IOException {
    if (CollectionUtils.isEmpty(params)) {
      return Collections.emptyMap();
    }

    Map<String, Object> map = new HashMap<>(params.size());
    Map<String, Object> bodyMap = getRequestBodyMap(request);
    for (ItemParam param : params) {
      String name = param.getName();
      ParamTypeEnum type = param.getType();
      ParamLocationEnum location = param.getLocation();
      Boolean isArray = param.getIsArray();
      Boolean required = param.getRequired();
      String defaultValue = param.getDefaultValue();
      if (location == ParamLocationEnum.REQUEST_HEADER) {
        List<Object> hv = Collections.list(request.getHeaders(name))
            .stream().map(v -> type.getConverter().apply(v))
            .collect(Collectors.toList());
        if (isArray) {
          if (CollectionUtils.isEmpty(hv)) {
            if (required) {
              invalidArgs.add(param);
            }
          } else {
            map.put(name, hv);
          }
        } else {
          if (CollectionUtils.isEmpty(hv)) {
            if (required) {
              invalidArgs.add(param);
            } else {
              map.put(name, type.getConverter().apply(defaultValue));
            }
          } else {
            map.put(name, hv.get(0));
          }
        }
      } else if (location == ParamLocationEnum.REQUEST_BODY) {
        Object paramValue = bodyMap.get(name);
        if (null == paramValue) {
          if (required) {
            invalidArgs.add(param);
          } else {
            if (!isArray) {
              map.put(name, type.getConverter().apply(defaultValue));
            }
          }
        } else {
          if (isArray) {
            List<Object> values = (paramValue instanceof List)
                ? (List) paramValue
                : Lists.newArrayList(paramValue);
            if (type.isObject()) {
              map.put(name, values);
            } else {
              List<Object> hv = values
                  .stream().map(v -> type.getConverter().apply(v.toString()))
                  .collect(Collectors.toList());
              map.put(name, hv);
            }
          } else {
            if (type.isObject()) {
              Map<String, Object> objectMap = (paramValue instanceof Map)
                  ? (Map<String, Object>) paramValue
                  : new HashMap<>();
              map.put(name, objectMap);
            } else {
              Object targetValue = (paramValue instanceof List)
                  ? ((List) paramValue).get(0)
                  : paramValue;
              map.put(name, type.getConverter().apply(targetValue.toString()));
            }
          }
        }
      } else {
        if (isArray) {
          String[] values = request.getParameterValues(name);
          if (ArrayUtils.isNotEmpty(values)) {
            List list = Arrays.asList(values).stream()
                .map(v -> type.getConverter().apply(v))
                .collect(Collectors.toList());
            map.put(name, list);
          } else {
            if (required) {
              invalidArgs.add(param);
            }
          }
        } else {
          String value = request.getParameter(name);
          if (StringUtils.isEmpty(value)) {
            if (required) {
              invalidArgs.add(param);
            } else {
              map.put(name, type.getConverter().apply(defaultValue));
            }
          } else {
            map.put(name, type.getConverter().apply(value));
          }
        }
      }
    }
    return map;
  }

  public Map<String, Object> getRequestBodyMap(HttpServletRequest request) throws IOException {
    HttpMethodEnum methodEnum = HttpMethodEnum.valueOf(request.getMethod());
    if (methodEnum.isHasBody() && null != request.getContentType()) {
      MediaType contentType = MediaType.parseMediaType(request.getContentType());
      Charset charset = (contentType != null && contentType.getCharset() != null ?
          contentType.getCharset() : StandardCharsets.UTF_8);
      for (HttpRequestBodyExtractor bodyExtractor : requestBodyExtractors) {
        if (bodyExtractor.support(contentType)) {
          return bodyExtractor.read(charset, request.getInputStream());
        }
      }
    }
    return Collections.emptyMap();
  }
}
