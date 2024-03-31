package com.gitee.sqlrest.core.exec;

import com.gitee.sqlrest.common.dto.ItemParam;
import com.gitee.sqlrest.common.dto.ResultEntity;
import com.gitee.sqlrest.common.enums.ParamTypeEnum;
import com.gitee.sqlrest.common.exception.ResponseErrorCode;
import com.gitee.sqlrest.core.driver.DriverLoadService;
import com.gitee.sqlrest.core.exec.engine.ApiExecutorEngineFactory;
import com.gitee.sqlrest.core.util.DataSourceUtils;
import com.gitee.sqlrest.persistence.dao.DataSourceDao;
import com.gitee.sqlrest.persistence.entity.ApiAssignmentEntity;
import com.gitee.sqlrest.persistence.entity.DataSourceEntity;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApiExecuteService {

  @Resource
  private DataSourceDao dataSourceDao;
  @Resource
  private DriverLoadService driverLoadService;

  public ResultEntity<Object> execute(ApiAssignmentEntity config, HttpServletRequest request,
      HttpServletResponse response) {
    DataSourceEntity dsEntity = dataSourceDao.getById(config.getDatasourceId());
    if (null == dsEntity) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      String message = "datasource[id=" + config.getDatasourceId() + " not exist!";
      log.warn("Error for handle api[id={}],information:{}", config.getId(), message);
      return ResultEntity.failed(ResponseErrorCode.ERROR_INTERNAL_ERROR, message);
    }
    Map<String, Object> paramValues = obtainParameterValues(request, config.getParams());
    File driverPath = driverLoadService.getVersionDriverFile(dsEntity.getType(), dsEntity.getVersion());
    HikariDataSource dataSource = DataSourceUtils.getHikariDataSource(dsEntity, driverPath.getAbsolutePath());
    Object result = ApiExecutorEngineFactory
        .getExecutor(config.getEngine(), dataSource, dsEntity.getType())
        .execute(config.getContextList(), paramValues);
    if (result instanceof Collection) {
      Collection r = (Collection) result;
      return ResultEntity.success(config.getContextList().size() == 1 ? r.stream().findFirst().get() : r);
    }
    return ResultEntity.success(result);
  }

  private Map<String, Object> obtainParameterValues(HttpServletRequest request, List<ItemParam> params) {
    Map<String, Object> map = new HashMap<>();
    if (null != params && params.size() > 0) {
      for (ItemParam param : params) {
        String name = param.getName();
        ParamTypeEnum type = param.getType();
        boolean isArray = Optional.ofNullable(param.getIsArray()).orElse(false);
        Boolean required = Optional.ofNullable(param.getRequired()).orElse(false);
        String defaultValue = param.getDefaultValue();
        if (isArray) {
          String[] values = request.getParameterValues(name);
          if (null != values && values.length > 0) {
            List list = Arrays.asList(values).stream()
                .map(v -> type.getConverter().apply(v))
                .collect(Collectors.toList());
            map.put(name, list);
          } else {
            map.put(name, null);
          }
        } else {
          String value = request.getParameter(name);
          if (!required && null == value) {
            value = defaultValue;
          }
          map.put(name, type.getConverter().apply(value));
        }
      }
    }
    return map;
  }

}
