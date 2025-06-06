package com.gitee.sqlrest.core.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import com.gitee.sqlrest.common.dto.ItemParam;
import com.gitee.sqlrest.common.dto.OutParam;
import com.gitee.sqlrest.common.dto.PageResult;
import com.gitee.sqlrest.common.dto.ParamValue;
import com.gitee.sqlrest.common.dto.ParamValue.BaseParamValue;
import com.gitee.sqlrest.common.dto.ResultEntity;
import com.gitee.sqlrest.common.enums.CacheKeyTypeEnum;
import com.gitee.sqlrest.common.enums.DataTypeFormatEnum;
import com.gitee.sqlrest.common.enums.NamingStrategyEnum;
import com.gitee.sqlrest.common.enums.OnOffEnum;
import com.gitee.sqlrest.common.enums.ParamLocationEnum;
import com.gitee.sqlrest.common.enums.ParamTypeEnum;
import com.gitee.sqlrest.common.exception.CommonException;
import com.gitee.sqlrest.common.exception.ResponseErrorCode;
import com.gitee.sqlrest.core.driver.DriverLoadService;
import com.gitee.sqlrest.core.dto.ApiAssignmentBaseResponse;
import com.gitee.sqlrest.core.dto.ApiAssignmentDetailResponse;
import com.gitee.sqlrest.core.dto.ApiAssignmentSaveRequest;
import com.gitee.sqlrest.core.dto.ApiDebugExecuteRequest;
import com.gitee.sqlrest.core.dto.AssignmentSearchRequest;
import com.gitee.sqlrest.core.dto.DataTypeFormatMapValue;
import com.gitee.sqlrest.core.dto.ExecuteSqlRecord;
import com.gitee.sqlrest.core.dto.ScriptEditorCompletion;
import com.gitee.sqlrest.core.dto.SqlParamParseResponse;
import com.gitee.sqlrest.core.exec.SqlExecuteLogger;
import com.gitee.sqlrest.core.exec.annotation.Comment;
import com.gitee.sqlrest.core.exec.engine.ApiExecutorEngineFactory;
import com.gitee.sqlrest.core.exec.engine.impl.ScriptExecutorService;
import com.gitee.sqlrest.core.util.ApiPathUtils;
import com.gitee.sqlrest.core.util.DataSourceUtils;
import com.gitee.sqlrest.core.util.JacksonUtils;
import com.gitee.sqlrest.persistence.dao.ApiAssignmentDao;
import com.gitee.sqlrest.persistence.dao.DataSourceDao;
import com.gitee.sqlrest.persistence.entity.ApiAssignmentEntity;
import com.gitee.sqlrest.persistence.entity.ApiContextEntity;
import com.gitee.sqlrest.persistence.entity.DataSourceEntity;
import com.gitee.sqlrest.persistence.util.PageUtils;
import com.gitee.sqlrest.template.XmlSqlTemplate;
import com.google.common.base.Charsets;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
                if (StringUtils.isBlank(subParamValue.getValue())) {
                  paramValue.setName(String.format("%s->%s", paramValue.getName(), subParamValue.getName()));
                  invalidArgs.add(paramValue);
                }
              }
            }
          }
        } else {
          if (paramValue.getRequired()) {
            if (StringUtils.isBlank(paramValue.getValue())) {
              invalidArgs.add(paramValue);
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

      Map<String, List<ParamValue>> names = request.getParamValues()
          .stream()
          .filter(
              i -> {
                if (i.getType().isObject()) {
                  return true;
                }
                return StringUtils.isNotBlank(i.getValue());
              }
          ).collect(Collectors.groupingBy(ParamValue::getName));
      for (Map.Entry<String, List<ParamValue>> entry : names.entrySet()) {
        String paramName = entry.getKey();
        List<ParamValue> value = entry.getValue();
        ParamTypeEnum type = value.get(0).getType();
        try {
          if (value.get(0).getIsArray()) {
            if (value.get(0).getType().isObject()) {
              List<Map<String, Object>> collection = new ArrayList<>();
              for (ParamValue pv : value) {
                if (pv.getType().isObject() && null != pv.getChildren()) {
                  Map<String, Object> objectMap = new HashMap<>(4);
                  for (BaseParamValue spv : pv.getChildren()) {
                    Object v = spv.getType().getConverter().apply(spv.getValue());
                    if (spv.getIsArray()) {
                      objectMap.put(spv.getName(), null == v ? null : Arrays.asList(v));
                    } else {
                      objectMap.put(spv.getName(), v);
                    }
                  }
                  if (objectMap.size() > 0) {
                    collection.add(objectMap);
                  }
                }
              }
              if (collection.size() > 0) {
                params.put(paramName, collection);
              }
            } else {
              List<Object> values = value.stream().map(ParamValue::getValue)
                  .map(s -> type.getConverter().apply(s))
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());
              params.put(paramName, values);
            }
          } else {
            if (value.get(0).getType().isObject()) {
              if (null != value.get(0).getChildren()) {
                Map<String, Object> objectMap = new HashMap<>(4);
                for (BaseParamValue spv : value.get(0).getChildren()) {
                  Object v = spv.getType().getConverter().apply(spv.getValue());
                  if (spv.getIsArray()) {
                    objectMap.put(spv.getName(), null == v ? null : Arrays.asList(v));
                  } else {
                    objectMap.put(spv.getName(), v);
                  }
                }
                params.put(paramName, objectMap);
              }
            } else {
              params.put(paramName, type.getConverter().apply(value.get(0).getValue()));
            }
          }
        } catch (Exception e) {
          throw new RuntimeException(String.format("[%s] value type invalid, %s", paramName, e.getMessage()));
        }
      }

      List<ParamValue> emptyList = request.getParamValues()
          .stream().filter(i -> StringUtils.isBlank(i.getValue()))
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
      SqlExecuteLogger.init();
      HikariDataSource dataSource = DataSourceUtils.getHikariDataSource(dataSourceEntity, driverPath.getAbsolutePath());
      List<Object> results = ApiExecutorEngineFactory
          .getExecutor(request.getEngine(), dataSource, dataSourceEntity.getType())
          .execute(scripts, params, request.getNamingStrategy());
      Object answer = results.size() > 1 ? results : (1 == results.size()) ? results.get(0) : null;
      List<OutParam> types = JacksonUtils.parseFieldTypes(results);
      String logs = Optional.ofNullable(SqlExecuteLogger.get())
          .orElseGet(ArrayList::new).stream().map(ExecuteSqlRecord::getDisplayText)
          .collect(Collectors.toList()).stream().collect(Collectors.joining("\n\n"));
      Map<String, Object> respMap = new HashMap<>(4);
      respMap.put("answer", answer);
      respMap.put("logs", logs);
      respMap.put("types", types);
      entity = ResultEntity.success(respMap);
    } catch (Exception e) {
      entity = ResultEntity.failed(ExceptionUtil.getMessage(e));
    } finally {
      SqlExecuteLogger.clear();
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

  public PageResult<ApiAssignmentBaseResponse> listAll(AssignmentSearchRequest request) {
    Supplier<List<ApiAssignmentBaseResponse>> method = () -> {
      List<ApiAssignmentEntity> lists = apiAssignmentDao
          .listAll(request.getGroupId(), request.getModuleId(),
              request.getPublish(), request.getOpen(),
              request.getSearchText());
      return lists.stream()
          .map(assignmentEntity -> {
            ApiAssignmentBaseResponse response = new ApiAssignmentBaseResponse();
            BeanUtil.copyProperties(assignmentEntity, response);
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
