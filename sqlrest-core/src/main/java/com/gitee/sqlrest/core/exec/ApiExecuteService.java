package com.gitee.sqlrest.core.exec;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.gitee.sqlrest.common.dto.ItemParam;
import com.gitee.sqlrest.common.dto.ResultEntity;
import com.gitee.sqlrest.common.enums.HttpMethodEnum;
import com.gitee.sqlrest.common.enums.ParamLocationEnum;
import com.gitee.sqlrest.common.enums.ParamTypeEnum;
import com.gitee.sqlrest.common.exception.ResponseErrorCode;
import com.gitee.sqlrest.core.driver.DriverLoadService;
import com.gitee.sqlrest.core.exec.engine.ApiExecutorEngineFactory;
import com.gitee.sqlrest.core.exec.extractor.HttpRequestBodyExtractor;
import com.gitee.sqlrest.core.util.DataSourceUtils;
import com.gitee.sqlrest.persistence.dao.DataSourceDao;
import com.gitee.sqlrest.persistence.entity.ApiAssignmentEntity;
import com.gitee.sqlrest.persistence.entity.DataSourceEntity;
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
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApiExecuteService {

  @Resource
  private DataSourceDao dataSourceDao;
  @Resource
  private DriverLoadService driverLoadService;
  @Resource
  private List<HttpRequestBodyExtractor> requestBodyExtractors;

  public ResultEntity<Object> execute(ApiAssignmentEntity config, HttpServletRequest request) {
    try {
      DataSourceEntity dsEntity = dataSourceDao.getById(config.getDatasourceId());
      if (null == dsEntity) {
        String message = "datasource[id=" + config.getDatasourceId() + " not exist!";
        log.warn("Error for handle api[id={}],information:{}", config.getId(), message);
        return ResultEntity.failed(ResponseErrorCode.ERROR_RESOURCE_NOT_EXISTS, message);
      }
      List<ItemParam> invalidArgs = new ArrayList<>();
      Map<String, Object> paramValues = extractParameters(request, config.getParams(), invalidArgs);
      if (invalidArgs.size() > 0) {
        return ResultEntity.failed(ResponseErrorCode.ERROR_INVALID_ARGUMENT, convertInvalidArgs(invalidArgs));
      }
      File driverPath = driverLoadService.getVersionDriverFile(dsEntity.getType(), dsEntity.getVersion());
      HikariDataSource dataSource = DataSourceUtils.getHikariDataSource(dsEntity, driverPath.getAbsolutePath());
      List<Object> results = ApiExecutorEngineFactory
          .getExecutor(config.getEngine(), dataSource, dsEntity.getType())
          .execute(config.getContextList(), paramValues, config.getNamingStrategy());
      return ResultEntity.success(results.size() > 1 ? results : results.get(0));
    } catch (Throwable t) {
      return ResultEntity.failed(ResponseErrorCode.ERROR_INTERNAL_ERROR, ExceptionUtil.getMessage(t));
    }
  }

  private String convertInvalidArgs(List<ItemParam> invalidArgs) {
    return "无效参数," + invalidArgs.stream().map(
        p -> (p.getIsArray() ? "数组" : "") + "参数'" + p.getName() + "'位于" + p.getLocation().getIn()
    ).collect(Collectors.joining(";"));
  }

  private Map<String, Object> extractParameters(HttpServletRequest request, List<ItemParam> params,
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
            if(required) {
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
            List<Object> hv = values
                .stream().map(v -> type.getConverter().apply(v.toString()))
                .collect(Collectors.toList());
            map.put(name, hv);
          } else {
            Object targetValue = (paramValue instanceof List)
                ? ((List) paramValue).get(0)
                : paramValue;
            map.put(name, type.getConverter().apply(targetValue.toString()));
          }
        }
      } else {
        if (isArray) {
          String[] values = request.getParameterValues(name);
          if (null != values && values.length > 0) {
            List list = Arrays.asList(values).stream()
                .map(v -> type.getConverter().apply(v))
                .collect(Collectors.toList());
            map.put(name, list);
          } else {
            if(required) {
              invalidArgs.add(param);
            }
          }
        } else {
          String value = request.getParameter(name);
          if (null == value) {
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
    if (methodEnum.isHasBody()) {
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
