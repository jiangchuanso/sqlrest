package com.gitee.sqlrest.core.filter;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.json.JSONUtil;
import com.gitee.sqlrest.common.consts.Constants;
import com.gitee.sqlrest.common.dto.ResultEntity;
import com.gitee.sqlrest.common.enums.HttpMethodEnum;
import com.gitee.sqlrest.common.exception.ResponseErrorCode;
import com.gitee.sqlrest.common.exception.UnAuthorizedException;
import com.gitee.sqlrest.common.exception.UnPermissionException;
import com.gitee.sqlrest.common.util.TokenUtils;
import com.gitee.sqlrest.core.servlet.ClientTokenService;
import com.gitee.sqlrest.core.util.ServletUtils;
import com.gitee.sqlrest.persistence.dao.ApiAssignmentDao;
import com.gitee.sqlrest.persistence.entity.AccessRecordEntity;
import com.gitee.sqlrest.persistence.entity.ApiAssignmentEntity;
import com.gitee.sqlrest.persistence.mapper.AccessRecordMapper;
import com.google.common.base.Charsets;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthenticationFilter implements Filter {

  @Resource
  private ApiAssignmentDao apiAssignmentDao;
  @Resource
  private FlowControlManger flowControlManger;
  @Resource
  private ClientTokenService clientTokenService;
  @Resource
  private AccessRecordMapper accessRecordMapper;

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
    ApiAssignmentEntity apiConfigEntity = apiAssignmentDao.getByUk(method, path, false);
    if (null == apiConfigEntity || !apiConfigEntity.getStatus()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      String message = String.format("/%s/%s[%s]", Constants.API_PATH_PREFIX, path, method.name());
      ResultEntity result = ResultEntity.failed(ResponseErrorCode.ERROR_PATH_NOT_EXISTS, message);
      response.getWriter().append(JSONUtil.toJsonStr(result));
      return;
    }

    if (apiConfigEntity.getFlowStatus()) {
      String resourceName = Constants.getResourceName(method.name(), path);
      if (flowControlManger.checkFlowControl(resourceName, response)) {
        doAuthenticationFilter(chain, request, response, apiConfigEntity);
      }
    } else {
      doAuthenticationFilter(chain, request, response, apiConfigEntity);
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
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      accessRecordEntity.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      ResultEntity resultEntity = ResultEntity.failed(ResponseErrorCode.ERROR_ACCESS_FORBIDDEN, e.getMessage());
      response.getWriter().append(JSONUtil.toJsonStr(resultEntity));
    } catch (UnPermissionException e) {
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
      accessRecordEntity.setDuration(System.currentTimeMillis() - accessRecordEntity.getDuration());
      CompletableFuture.runAsync(() -> accessRecordMapper.insert(accessRecordEntity));
    }
  }

  @Override
  public void destroy() {

  }
}
