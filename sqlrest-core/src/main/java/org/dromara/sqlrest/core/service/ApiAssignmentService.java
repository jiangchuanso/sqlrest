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
import cn.hutool.core.exceptions.ExceptionUtil;
import org.dromara.sqlrest.common.dto.ItemParam;
import org.dromara.sqlrest.common.dto.OutParam;
import org.dromara.sqlrest.common.dto.PageResult;
import org.dromara.sqlrest.common.dto.ParamValue;
import org.dromara.sqlrest.common.dto.ParamValue.BaseParamValue;
import org.dromara.sqlrest.common.dto.ResultEntity;
import org.dromara.sqlrest.common.enums.CacheKeyTypeEnum;
import org.dromara.sqlrest.common.enums.DataTypeFormatEnum;
import org.dromara.sqlrest.common.enums.NamingStrategyEnum;
import org.dromara.sqlrest.common.enums.OnOffEnum;
import org.dromara.sqlrest.common.enums.ParamLocationEnum;
import org.dromara.sqlrest.common.enums.ParamTypeEnum;
import org.dromara.sqlrest.common.exception.CommonException;
import org.dromara.sqlrest.common.exception.ResponseErrorCode;
import org.dromara.sqlrest.common.service.DisplayRecord;
import org.dromara.sqlrest.core.driver.DriverLoadService;
import org.dromara.sqlrest.core.dto.ApiAssignmentBaseResponse;
import org.dromara.sqlrest.core.dto.ApiAssignmentDetailResponse;
import org.dromara.sqlrest.core.dto.ApiAssignmentSaveRequest;
import org.dromara.sqlrest.core.dto.ApiDebugExecuteRequest;
import org.dromara.sqlrest.core.dto.AssignmentSearchRequest;
import org.dromara.sqlrest.core.dto.DataTypeFormatMapValue;
import org.dromara.sqlrest.core.dto.ScriptEditorCompletion;
import org.dromara.sqlrest.core.dto.SqlParamParseResponse;
import org.dromara.sqlrest.core.exec.logger.DebugExecuteLogger;
import org.dromara.sqlrest.core.exec.annotation.Comment;
import org.dromara.sqlrest.core.exec.engine.ApiExecutorEngineFactory;
import org.dromara.sqlrest.core.exec.engine.impl.ScriptExecutorService;
import org.dromara.sqlrest.core.util.ApiPathUtils;
import org.dromara.sqlrest.core.util.DataSourceUtils;
import org.dromara.sqlrest.core.util.JacksonUtils;
import org.dromara.sqlrest.persistence.dao.ApiAssignmentDao;
import org.dromara.sqlrest.persistence.dao.ApiGroupDao;
import org.dromara.sqlrest.persistence.dao.ApiModuleDao;
import org.dromara.sqlrest.persistence.dao.DataSourceDao;
import org.dromara.sqlrest.persistence.entity.ApiAssignmentEntity;
import org.dromara.sqlrest.persistence.entity.ApiContextEntity;
import org.dromara.sqlrest.persistence.entity.ApiGroupEntity;
import org.dromara.sqlrest.persistence.entity.ApiModuleEntity;
import org.dromara.sqlrest.persistence.entity.DataSourceEntity;
import org.dromara.sqlrest.persistence.util.PageUtils;
import org.dromara.sqlrest.template.XmlSqlTemplate;
import com.google.common.base.Charsets;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class ApiAssignmentService {

  private final static Map<String, List<ScriptEditorCompletion>> memCache = new ConcurrentHashMap<>();

  @Resource
  public ApiAssignmentDao apiAssignmentDao;
  @Resource
  private DataSourceDao dataSourceDao;
  @Resource
  private ApiGroupDao apiGroupDao;
  @Resource
  private ApiModuleDao apiModuleDao;
  @Resource
  private DriverLoadService driverLoadService;

  public List<ScriptEditorCompletion> completions() {
    return memCache.computeIfAbsent("COMPLETION", this::computeCompletions);
  }

  private List<ScriptEditorCompletion> computeCompletions(String key) {
    List<ScriptEditorCompletion> results = new ArrayList<>();
    results.addAll(ScriptExecutorService.syntax);

    for (Class clazz : ScriptExecutorService.modules) {
      String varName = ScriptExecutorService.getModuleVarName(clazz);
      for (Method method : clazz.getMethods()) {
        if (method.isAnnotationPresent(Comment.class)) {
          String methodName = method.getName();
          String params = Stream.of(method.getParameters())
              .map(item -> {
                String type = item.getType().getSimpleName();
                String name = item.isAnnotationPresent(Comment.class)
                    ? item.getAnnotation(Comment.class).value()
                    : item.getName();
                return type + " " + name;
              })
              .collect(Collectors.joining(","));
          results.add(ScriptEditorCompletion.builder()
              .meta(method.getReturnType().getName())
              .caption(String.format("%s.%s(%s)", varName, methodName, params))
              .value(String.format("%s.%s( )", varName, methodName))
              .build());
        }
      }
    }

    return results;
  }

  public List<SqlParamParseResponse> parseSqlParams(String sqlOrXml) {
    XmlSqlTemplate template = new XmlSqlTemplate(sqlOrXml);
    List<SqlParamParseResponse> results = new LinkedList<>();
    Map<String, Boolean> names = template.getParameterNames();
    // 先处理没有点号的常规参数
    names.forEach(
        (name, isArray) -> {
          if (!name.contains(".")) {
            results.add(new SqlParamParseResponse(name, isArray));
          }
        }
    );
    // 再处理有点号的对象参数
    names.forEach(
        (name, isArray) -> {
          if (name.contains(".")) {
            int idx = name.indexOf(".");
            String objName = name.substring(0, idx);
            String subName = name.substring(idx + 1);
            Optional<SqlParamParseResponse> parentParam = results.stream()
                .filter(it -> objName.equals(it.getName()))
                .findFirst();
            if (parentParam.isPresent()) {
              parentParam.get().getChildren().add(new SqlParamParseResponse(subName, isArray));
            }
          }
        }
    );
    return results;
  }

  public void debugExecute(ApiDebugExecuteRequest request, HttpServletResponse response) throws IOException {
    DataSourceEntity dataSourceEntity = dataSourceDao.getById(request.getDataSourceId());
    if (null == dataSourceEntity) {
      String message = "datasource[id=" + request.getDataSourceId() + " not exist!";
      log.warn("Error for debug, information:{}", message);
      throw new CommonException(ResponseErrorCode.ERROR_INTERNAL_ERROR, message);
    }
    if (CollectionUtils.isEmpty(request.getContextList())) {
      throw new CommonException(ResponseErrorCode.ERROR_INTERNAL_ERROR, "contextList invalid argument");
    }
    List<ApiContextEntity> scripts = request.getContextList().stream()
        .map(str -> ApiContextEntity.builder().sqlText(str).build())
        .collect(Collectors.toList());
    Map<String, Object> params = new HashMap<>();
    if (!CollectionUtils.isEmpty(request.getParamValues())) {
      List<ParamValue> invalidArgs = new ArrayList<>();
      for (ParamValue paramValue : request.getParamValues()) {
        if (StringUtils.isBlank(paramValue.getName())) {
          throw new CommonException(ResponseErrorCode.ERROR_INTERNAL_ERROR, "parameter name must is not blank");
        }
        if (paramValue.getType().isObject()) {
          if (null != paramValue.getChildren()) {
            for (BaseParamValue subParamValue : paramValue.getChildren()) {
              if (StringUtils.isBlank(subParamValue.getName())) {
                throw new CommonException(ResponseErrorCode.ERROR_INTERNAL_ERROR, "parameter name must is not blank");
              }
              if (subParamValue.getRequired()) {
                if (subParamValue.getIsArray()) {
                  if (CollectionUtils.isEmpty(subParamValue.getArrayValues())) {
                    paramValue.setName(String.format("%s->%s", paramValue.getName(), subParamValue.getName()));
                    invalidArgs.add(paramValue);
                  }
                } else {
                  if (StringUtils.isBlank(subParamValue.getValue())) {
                    paramValue.setName(String.format("%s->%s", paramValue.getName(), subParamValue.getName()));
                    invalidArgs.add(paramValue);
                  }
                }
              }
            }
          }
        } else {
          if (paramValue.getRequired()) {
            if (paramValue.getIsArray()) {
              if (CollectionUtils.isEmpty(paramValue.getArrayValues())) {
                invalidArgs.add(paramValue);
              }
            } else {
              if (StringUtils.isBlank(paramValue.getValue())) {
                invalidArgs.add(paramValue);
              }
            }
          }
        }
      }
      if (invalidArgs.size() > 0) {
        String msg = "必填参数为空," + invalidArgs.stream().map(
            p -> (p.getIsArray() ? "数组" : "") + "参数'" + p.getName()
        ).collect(Collectors.joining(";"));
        throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT, msg);
      }

      for (ParamValue param : request.getParamValues()) {
        String paramName = param.getName();
        ParamTypeEnum type = param.getType();
        try {
          if (param.getIsArray()) {
            if (param.getType().isObject()) {
              List<Map<String, Object>> collection = new ArrayList<>();
              if (null != param.getChildren()) {
                Map<String, Object> objectMap = new HashMap<>(param.getChildren().size());
                for (BaseParamValue spv : param.getChildren()) {
                  if (spv.getIsArray()) {
                    List<Object> ls = CollectionUtils.isEmpty(spv.getArrayValues())
                        ? null
                        : spv.getArrayValues().stream()
                            .map(item -> spv.getType().getConverter().apply(item))
                            .collect(Collectors.toList());
                    objectMap.put(spv.getName(), ls);
                  } else {
                    String value = spv.getRequired()
                        ? spv.getValue()
                        : (StringUtils.isNotBlank(spv.getValue()) ? spv.getValue() : spv.getDefaultValue());
                    Object v = spv.getType().getConverter().apply(value);
                    objectMap.put(spv.getName(), v);
                  }
                }
                if (objectMap.size() > 0) {
                  collection.add(objectMap);
                }
              }
              if (collection.size() > 0) {
                params.put(paramName, collection);
              }
            } else {
              List<Object> ls = CollectionUtils.isEmpty(param.getArrayValues())
                  ? null
                  : param.getArrayValues().stream()
                      .map(item -> type.getConverter().apply(item))
                      .collect(Collectors.toList());
              params.put(paramName, ls);
            }
          } else {
            if (type.isObject()) {
              if (null != param.getChildren()) {
                Map<String, Object> objectMap = new HashMap<>(4);
                for (BaseParamValue spv : param.getChildren()) {
                  if (spv.getIsArray()) {
                    List<Object> ls = CollectionUtils.isEmpty(spv.getArrayValues())
                        ? null
                        : spv.getArrayValues().stream()
                            .map(item -> spv.getType().getConverter().apply(item))
                            .collect(Collectors.toList());
                    objectMap.put(spv.getName(), ls);
                  } else {
                    String value = spv.getRequired()
                        ? spv.getValue()
                        : (StringUtils.isNotBlank(spv.getValue()) ? spv.getValue() : spv.getDefaultValue());
                    Object v = spv.getType().getConverter().apply(value);
                    objectMap.put(spv.getName(), v);
                  }
                }
                params.put(paramName, objectMap);
              }
            } else {
              String value = param.getRequired()
                  ? param.getValue()
                  : (StringUtils.isNotBlank(param.getValue()) ? param.getValue() : param.getDefaultValue());
              params.put(paramName, type.getConverter().apply(value));
            }
          }
        } catch (Exception e) {
          throw new RuntimeException(String.format("[%s] value type invalid, %s", paramName, e.getMessage()));
        }
      }

      List<ParamValue> emptyList = request.getParamValues()
          .stream().filter(i -> !i.getIsArray()).filter(i -> StringUtils.isBlank(i.getValue()))
          .collect(Collectors.toList());
      for (ParamValue paramValue : emptyList) {
        ParamTypeEnum type = paramValue.getType();
        if (!params.containsKey(paramValue.getName())) {
          if (!paramValue.getRequired()) {
            if (paramValue.getIsArray()) {
              params.put(paramValue.getName(), Collections.emptyList());
            } else {
              params.put(paramValue.getName(), type.getConverter().apply(paramValue.getDefaultValue()));
            }
          }
        }
      }
    }

    if (null == request.getNamingStrategy()) {
      request.setNamingStrategy(NamingStrategyEnum.CAMEL_CASE);
    }

    File driverPath = driverLoadService.getVersionDriverFile(dataSourceEntity.getType(), dataSourceEntity.getVersion());

    ResultEntity entity;
    try {
      DebugExecuteLogger.init();
      HikariDataSource dataSource = DataSourceUtils.getHikariDataSource(dataSourceEntity, driverPath.getAbsolutePath());
      List<Object> results = ApiExecutorEngineFactory
          .getExecutor(request.getEngine(), dataSource, dataSourceEntity.getType(), true)
          .execute(scripts, params, request.getNamingStrategy());
      Object answer = results.size() > 1 ? results : (1 == results.size()) ? results.get(0) : null;
      List<OutParam> types = JacksonUtils.parseFiledTypesAndFillNullAsString(results);
      String logs = Optional.ofNullable(DebugExecuteLogger.get())
          .orElseGet(ArrayList::new).stream().map(DisplayRecord::getDisplayText)
          .collect(Collectors.toList()).stream().collect(Collectors.joining("\n\n"));
      Map<String, Object> respMap = new HashMap<>(4);
      respMap.put("answer", answer);
      respMap.put("logs", logs);
      respMap.put("types", types);
      entity = ResultEntity.success(respMap);
    } catch (Exception e) {
      log.warn("Failed to debug for error:{}", e.getMessage(), e);
      entity = ResultEntity.failed(ExceptionUtil.getMessage(e));
    } finally {
      DebugExecuteLogger.clear();
    }

    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(Charsets.UTF_8.name());

    Map<DataTypeFormatEnum, String> formatMap = request.getFormatMap().stream()
        .collect(
            Collectors.toMap(
                DataTypeFormatMapValue::getKey,
                DataTypeFormatMapValue::getValue,
                (a, b) -> a));
    response.getWriter().append(JacksonUtils.toJsonStr(entity, formatMap));
  }

  public Long createAssignment(ApiAssignmentSaveRequest request) {
    if (StringUtils.isBlank(request.getPath()) || null == request.getMethod()) {
      throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT, "path or method");
    }
    if (null != apiAssignmentDao.getByUk(request.getMethod(), request.getPath())) {
      String message = String.format("path=[%s]%s", request.getMethod().name(), request.getPath());
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_ALREADY_EXISTS, message);
    }
    if (!CollectionUtils.isEmpty(request.getParams())) {
      if (!request.getMethod().isHasBody()) {
        if (request.getParams().stream().anyMatch(i -> ParamLocationEnum.REQUEST_BODY == i.getLocation())) {
          throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT,
              "Request with GET/HEAD method cannot have body.");
        }
      }

      for (ItemParam itemParam : request.getParams()) {
        itemParam.checkValid(request.getMethod());
      }
    }
    if (!CollectionUtils.isEmpty(request.getOutputs())) {
      for (OutParam outParam : request.getOutputs()) {
        outParam.checkValid();
      }
    }
    if (null == request.getDatasourceId() || null == dataSourceDao.getById(request.getDatasourceId())) {
      throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT,
          "Invalid datasourceId or maybe not exist.");
    }
    if (CollectionUtils.isEmpty(request.getContextList())) {
      throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT, "contextList");
    }
    if (null == request.getNamingStrategy()) {
      request.setNamingStrategy(NamingStrategyEnum.CAMEL_CASE);
    }
    if (null == request.getCacheKeyType()) {
      request.setCacheKeyType(CacheKeyTypeEnum.NONE);
    }
    while (request.getPath().startsWith("/")) {
      request.setPath(request.getPath().substring(1));
    }

    if (request.getCacheKeyType().isUseCache()) {
      if (request.getCacheExpireSeconds() <= 0) {
        throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT, "Invalid param cacheExpireSeconds");
      }
      if (CacheKeyTypeEnum.SPEL == request.getCacheKeyType()) {
        if (StringUtils.isBlank(request.getCacheKeyExpr())) {
          throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT, "SPEL cache must has cacheKeyExpr");
        }
      }
      if (CacheKeyTypeEnum.AUTO == request.getCacheKeyType()) {
        request.setCacheKeyExpr(null);
      }
    } else {
      request.setCacheExpireSeconds(0L);
      request.setCacheKeyExpr(null);
    }

    List<ApiContextEntity> contextList = getContextListEntity(request.getContextList());

    ApiAssignmentEntity assignmentEntity = new ApiAssignmentEntity();
    assignmentEntity.setGroupId(request.getGroupId());
    assignmentEntity.setModuleId(request.getModuleId());
    assignmentEntity.setDatasourceId(request.getDatasourceId());
    assignmentEntity.setName(request.getName());
    assignmentEntity.setDescription(request.getDescription());
    assignmentEntity.setMethod(request.getMethod());
    assignmentEntity.setPath(request.getPath());
    assignmentEntity.setContentType(request.getContentType());
    assignmentEntity.setParams(request.getParams());
    assignmentEntity.setOutputs(request.getOutputs());
    assignmentEntity.setOpen(Optional.ofNullable(request.getOpen()).orElse(false));
    assignmentEntity.setEngine(request.getEngine());
    assignmentEntity.setStatus(false);
    assignmentEntity.setContextList(contextList);
    assignmentEntity.setResponseFormat(request.getFormatMap().stream()
        .collect(Collectors.toMap(DataTypeFormatMapValue::getKey, DataTypeFormatMapValue::getValue, (a, b) -> a)));
    assignmentEntity.setNamingStrategy(request.getNamingStrategy());
    assignmentEntity.setFlowStatus(Optional.ofNullable(request.getFlowStatus()).orElse(false));
    assignmentEntity.setFlowGrade(request.getFlowGrade());
    assignmentEntity.setFlowCount(request.getFlowCount());
    assignmentEntity.setCacheKeyType(request.getCacheKeyType());
    assignmentEntity.setCacheKeyExpr(request.getCacheKeyExpr());
    assignmentEntity.setCacheExpireSeconds(Optional.ofNullable(request.getCacheExpireSeconds()).orElse(0L));

    apiAssignmentDao.insert(assignmentEntity);
    return assignmentEntity.getId();
  }

  public void updateAssignment(ApiAssignmentSaveRequest request) {
    ApiAssignmentEntity exists = apiAssignmentDao.getById(request.getId(), false);
    if (null == exists) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_NOT_EXISTS, "id=" + request.getId());
    }
    if (exists.getMethod() != request.getMethod()) {
      throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT, "can't update method");
    }
    if (!StringUtils.equals(exists.getPath(), request.getPath())) {
      throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT, "can't update path");
    }
    if (exists.getStatus()) {
      throw new CommonException(ResponseErrorCode.ERROR_EDIT_ALREADY_PUBLISHED, "id=" + request.getId());
    }
    if (StringUtils.isBlank(request.getPath()) || null == request.getMethod()) {
      throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT, "path or method");
    }
    if (CollectionUtils.isEmpty(request.getContextList())) {
      throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT, "sqlTextList");
    }
    if (!CollectionUtils.isEmpty(request.getParams())) {
      if (!request.getMethod().isHasBody()) {
        if (request.getParams().stream().anyMatch(i -> ParamLocationEnum.REQUEST_BODY == i.getLocation())) {
          throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT,
              "Request with GET/HEAD method cannot have body.");
        }
      }
      for (ItemParam itemParam : request.getParams()) {
        itemParam.checkValid(exists.getMethod());
      }
    }
    if (!CollectionUtils.isEmpty(request.getOutputs())) {
      for (OutParam outParam : request.getOutputs()) {
        outParam.checkValid();
      }
    }
    if (null == request.getDatasourceId() || null == dataSourceDao.getById(request.getDatasourceId())) {
      throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT,
          "Invalid datasourceId or maybe not exist.");
    }
    if (null == request.getNamingStrategy()) {
      request.setNamingStrategy(NamingStrategyEnum.CAMEL_CASE);
    }
    if (null == request.getCacheKeyType()) {
      request.setCacheKeyType(CacheKeyTypeEnum.NONE);
    }

    if (request.getCacheKeyType().isUseCache()) {
      if (request.getCacheExpireSeconds() <= 0) {
        throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT, "Invalid param cacheExpireSeconds");
      }
      if (CacheKeyTypeEnum.SPEL == request.getCacheKeyType()) {
        if (StringUtils.isBlank(request.getCacheKeyExpr())) {
          throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT, "SPEL cache must has cacheKeyExpr");
        }
      }
      if (CacheKeyTypeEnum.AUTO == request.getCacheKeyType()) {
        request.setCacheKeyExpr(null);
      }
    } else {
      request.setCacheExpireSeconds(0L);
      request.setCacheKeyExpr(null);
    }

    List<ApiContextEntity> contextList = getContextListEntity(request.getContextList());

    ApiAssignmentEntity assignmentEntity = new ApiAssignmentEntity();
    assignmentEntity.setId(request.getId());
    assignmentEntity.setGroupId(request.getGroupId());
    assignmentEntity.setModuleId(request.getModuleId());
    assignmentEntity.setDatasourceId(request.getDatasourceId());
    assignmentEntity.setName(request.getName());
    assignmentEntity.setDescription(request.getDescription());
    //assignmentEntity.setMethod(request.getMethod());
    //assignmentEntity.setPath(request.getPath());
    assignmentEntity.setContentType(request.getContentType());
    assignmentEntity.setParams(request.getParams());
    assignmentEntity.setOutputs(request.getOutputs());
    assignmentEntity.setOpen(Optional.ofNullable(request.getOpen()).orElse(false));
    assignmentEntity.setStatus(false);
    assignmentEntity.setEngine(request.getEngine());
    assignmentEntity.setContextList(contextList);
    assignmentEntity.setResponseFormat(request.getFormatMap().stream()
        .collect(Collectors.toMap(DataTypeFormatMapValue::getKey, DataTypeFormatMapValue::getValue, (a, b) -> a)));
    assignmentEntity.setNamingStrategy(request.getNamingStrategy());
    assignmentEntity.setFlowStatus(Optional.ofNullable(request.getFlowStatus()).orElse(false));
    assignmentEntity.setFlowGrade(request.getFlowGrade());
    assignmentEntity.setFlowCount(request.getFlowCount());
    assignmentEntity.setCacheKeyType(request.getCacheKeyType());
    assignmentEntity.setCacheKeyExpr(request.getCacheKeyExpr());
    assignmentEntity.setCacheExpireSeconds(Optional.ofNullable(request.getCacheExpireSeconds()).orElse(0L));

    apiAssignmentDao.update(assignmentEntity);
  }

  public ApiAssignmentDetailResponse detailAssignment(Long id) {
    ApiAssignmentEntity assignmentEntity = apiAssignmentDao.getById(id, true);
    if (null == assignmentEntity) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_NOT_EXISTS, "id=" + id);
    }

    ApiAssignmentDetailResponse response = new ApiAssignmentDetailResponse();
    BeanUtil.copyProperties(assignmentEntity, response);
    response.setSqlList(assignmentEntity.getContextList());
    List<DataTypeFormatMapValue> formatMap = new ArrayList<>();

    for (Map.Entry<DataTypeFormatEnum, String> entry : assignmentEntity.getResponseFormat().entrySet()) {
      formatMap.add(
          DataTypeFormatMapValue.builder()
              .key(entry.getKey())
              .value(entry.getValue())
              .remark(entry.getKey().getClassName())
              .build());
    }
    response.setFormatMap(formatMap);
    return response;
  }

  public void deleteAssignment(Long id) {
    ApiAssignmentEntity assignmentEntity = apiAssignmentDao.getById(id, false);
    if (null != assignmentEntity) {
      if (OnOffEnum.ON.equals(assignmentEntity.getStatus())) {
        throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_ALREADY_USED, "api assignment is online.");
      }
      apiAssignmentDao.deleteById(id);
    }
  }

  public void makeOpen(Long id, Boolean open) {
    apiAssignmentDao.makeOpen(id, open);
  }

  public void makeAlarm(Long id, Boolean open) {
    apiAssignmentDao.makeAlarm(id, open);
  }

  public void deployAssignment(Long id) {
    apiAssignmentDao.updateStatus(id, true);
  }

  public void retireAssignment(Long id) {
    apiAssignmentDao.updateStatus(id, false);
  }

  @Transactional(rollbackFor = Exception.class)
  public void updateGroup(Long groupId, List<Long> ids) {
    if (null == groupId || null == apiGroupDao.getById(groupId)) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_NOT_EXISTS, "groupId=" + groupId);
    }
    apiAssignmentDao.resetGroupByGroupId(groupId);
    apiAssignmentDao.updateGroup(groupId, ids);
  }

  public PageResult<ApiAssignmentBaseResponse> listAll(AssignmentSearchRequest request) {
    Map<Long, String> moduleIdNameMap = apiModuleDao.listAll().stream()
        .collect(Collectors.toMap(ApiModuleEntity::getId, ApiModuleEntity::getName));
    Map<Long, String> groupIdNameMap = apiGroupDao.listAll().stream()
        .collect(Collectors.toMap(ApiGroupEntity::getId, ApiGroupEntity::getName));
    Supplier<List<ApiAssignmentBaseResponse>> method = () -> {
      List<ApiAssignmentEntity> lists = apiAssignmentDao
          .listAll(request.getGroupId(), request.getModuleId(),
              request.getPublish(), request.getOpen(),
              request.getSearchText());
      return lists.stream()
          .map(assignmentEntity -> {
            ApiAssignmentBaseResponse response = new ApiAssignmentBaseResponse();
            BeanUtil.copyProperties(assignmentEntity, response);
            response.setModuleName(moduleIdNameMap.get(assignmentEntity.getModuleId()));
            response.setGroupName(groupIdNameMap.get(assignmentEntity.getGroupId()));
            response.setPath(ApiPathUtils.getFullPath(response.getPath()));
            return response;
          })
          .collect(Collectors.toList());
    };

    return PageUtils.getPage(method, request.getPage(), request.getSize());
  }

  private List<ApiContextEntity> getContextListEntity(List<String> contextList) {
    List<ApiContextEntity> sqlList = contextList.stream()
        .filter(StringUtils::isNotBlank)
        .map(s -> new ApiContextEntity(s))
        .collect(Collectors.toList());
    return sqlList;
  }

}
