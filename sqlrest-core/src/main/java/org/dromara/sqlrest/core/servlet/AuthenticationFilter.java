// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.servlet;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.json.JSONUtil;
import com.google.common.base.Charsets;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.sqlrest.common.consts.Constants;
import org.dromara.sqlrest.common.dto.ResultEntity;
import org.dromara.sqlrest.common.enums.HttpMethodEnum;
import org.dromara.sqlrest.common.exception.ResponseErrorCode;
import org.dromara.sqlrest.common.exception.UnAuthorizedException;
import org.dromara.sqlrest.common.exception.UnPermissionException;
import org.dromara.sqlrest.common.service.FlowControlManger;
import org.dromara.sqlrest.common.util.InetUtils;
import org.dromara.sqlrest.common.util.TokenUtils;
import org.dromara.sqlrest.core.exec.ApiAssignmentCache;
import org.dromara.sqlrest.core.exec.logger.RequestParamLogger;
import org.dromara.sqlrest.core.executor.UnifyAlarmOpsService;
import org.dromara.sqlrest.core.util.AlarmModelUtils;
import org.dromara.sqlrest.core.util.ServletUtils;
import org.dromara.sqlrest.persistence.dao.ApiOnlineDao;
import org.dromara.sqlrest.persistence.entity.AccessRecordEntity;
import org.dromara.sqlrest.persistence.entity.ApiAssignmentEntity;
import org.dromara.sqlrest.persistence.mapper.AccessRecordMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthenticationFilter implements Filter {

  private static final ExecutorService alarmExecutor = ExecutorBuilder.create()
      .setCorePoolSize(Runtime.getRuntime().availableProcessors())
      .setMaxPoolSize(5 * Runtime.getRuntime().availableProcessors())
      .useArrayBlockingQueue(8912)
      .setHandler(new CallerRunsPolicy())
      .build();

  @Resource
  private ApiOnlineDao apiOnlineDao;
  @Resource
  private FlowControlManger flowControlManger;
  @Resource
  private ClientTokenService clientTokenService;
  @Resource
  private AccessRecordMapper accessRecordMapper;
  @Resource
  private UnifyAlarmOpsService unifyAlarmOpsService;

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) resp;
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(Charsets.UTF_8.name());
    String path = request.getRequestURI().substring(Constants.API_PATH_PREFIX.length() + 2);
    HttpMethodEnum method = HttpMethodEnum.exists(request.getMethod())
        ? HttpMethodEnum.valueOf(request.getMethod().toUpperCase())
        : HttpMethodEnum.GET;
    ApiAssignmentEntity apiConfigEntity = apiOnlineDao.getByUk(method, path);
    if (null == apiConfigEntity) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      String message = String.format("/%s/%s[%s]", Constants.API_PATH_PREFIX, path, method.name());
      ResultEntity result = ResultEntity.failed(ResponseErrorCode.ERROR_PATH_NOT_EXISTS, message);
      response.getWriter().append(JSONUtil.toJsonStr(result));
      log.warn("Request path not exists: {}", message);
      return;
    }

    try {
      ApiAssignmentCache.set(apiConfigEntity);

      if (apiConfigEntity.getFlowStatus()) {
        String resourceName = Constants.getResourceName(method.name(), path);
        if (flowControlManger.checkFlowControl(resourceName, response)) {
          doAuthenticationFilter(chain, request, response, apiConfigEntity);
        }
      } else {
        doAuthenticationFilter(chain, request, response, apiConfigEntity);
      }
    } finally {
      ApiAssignmentCache.remove();
    }
  }

  private void doAuthenticationFilter(FilterChain chain, HttpServletRequest request, HttpServletResponse response,
      ApiAssignmentEntity apiConfigEntity) throws IOException {
    AccessRecordEntity accessRecordEntity = AccessRecordEntity.builder()
        .path(request.getRequestURI())
        .status(HttpStatus.OK.value())
        .duration(System.currentTimeMillis())
        .ipAddr(ServletUtils.getIpAddr())
        .userAgent(ServletUtils.getUserAgent())
        .apiId(apiConfigEntity.getId())
        .executorAddr(InetUtils.getLocalIpStr())
        .gatewayAddr(request.getHeader(Constants.REQUEST_HEADER_GATEWAY_IP))
        .build();

    String path = apiConfigEntity.getPath();
    HttpMethodEnum method = apiConfigEntity.getMethod();

    try {
      if (!apiConfigEntity.getOpen()) {
        String tokenStr = TokenUtils.getRequestToken(request);
        if (StringUtils.isBlank(tokenStr)) {
          throw new UnAuthorizedException("Need bearer token.");
        }
        String appKey = clientTokenService.verifyTokenAndGetAppKey(tokenStr);
        accessRecordEntity.setClientKey(appKey);
        if (null == appKey) {
          log.error("Failed get app key from token [{}], maybe is invalid or expired. ", tokenStr);
          throw new UnAuthorizedException("Invalid or Expired Token : " + tokenStr);
        } else {
          boolean verify = clientTokenService.verifyAuthGroup(appKey, apiConfigEntity.getGroupId());
          if (!verify) {
            log.error("Failed verify group from token [{}] , app key [{}].", tokenStr, appKey);
            String message = String.format("/%s/%s[%s]", Constants.API_PATH_PREFIX, path, method.name());
            throw new UnPermissionException("No Permission to access: " + message);
          }
        }
      }
      chain.doFilter(request, response);
    } catch (UnAuthorizedException e) {
      accessRecordEntity.setException(e.getMessage());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      accessRecordEntity.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      ResultEntity resultEntity = ResultEntity.failed(ResponseErrorCode.ERROR_ACCESS_FORBIDDEN, e.getMessage());
      response.getWriter().append(JSONUtil.toJsonStr(resultEntity));
    } catch (UnPermissionException e) {
      accessRecordEntity.setException(e.getMessage());
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      accessRecordEntity.setStatus(HttpServletResponse.SC_FORBIDDEN);
      ResultEntity resultEntity = ResultEntity.failed(ResponseErrorCode.ERROR_ACCESS_FORBIDDEN, e.getMessage());
      response.getWriter().append(JSONUtil.toJsonStr(resultEntity));
    } catch (Throwable t) {
      String exception = (null != t.getMessage()) ? t.getMessage() : ExceptionUtil.stacktraceToString(t, 100);
      accessRecordEntity.setException(exception);
      ResultEntity resultEntity = ResultEntity.failed(ResponseErrorCode.ERROR_INTERNAL_ERROR, exception);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      accessRecordEntity.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.getWriter().append(JSONUtil.toJsonStr(resultEntity));
    } finally {
      final long accessTime = accessRecordEntity.getDuration();
      final int httpStatus = response.getStatus();
      accessRecordEntity.setDuration(System.currentTimeMillis() - accessRecordEntity.getDuration());
      accessRecordEntity.setParameters(RequestParamLogger.getAndClear());
      alarmExecutor.submit(() -> doRecord(apiConfigEntity, accessRecordEntity, httpStatus, accessTime));
    }
  }

  private void doRecord(ApiAssignmentEntity config, AccessRecordEntity record, int status, long timestamp) {
    accessRecordMapper.insert(record);
    if (status == HttpServletResponse.SC_OK) {
      return;
    }
    if (!config.getAlarm()) {
      return;
    }

    Map<String, String> dataModel = AlarmModelUtils.getBusinessModel(config, record, timestamp);
    unifyAlarmOpsService.triggerAlarm(dataModel);
  }

  @Override
  public void destroy() {

  }
}
