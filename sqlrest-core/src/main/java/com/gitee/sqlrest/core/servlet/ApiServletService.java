package com.gitee.sqlrest.core.servlet;

import cn.hutool.json.JSONUtil;
import com.gitee.sqlrest.common.consts.Constants;
import com.gitee.sqlrest.common.dto.ResultEntity;
import com.gitee.sqlrest.common.enums.HttpMethodEnum;
import com.gitee.sqlrest.common.exception.ResponseErrorCode;
import com.gitee.sqlrest.core.exec.ApiExecuteService;
import com.gitee.sqlrest.persistence.dao.ApiAssignmentDao;
import com.gitee.sqlrest.persistence.entity.ApiAssignmentEntity;
import java.io.IOException;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApiServletService {

  @Resource
  private ApiAssignmentDao apiAssignmentDao;
  @Resource
  private ApiExecuteService apiExecuteService;

  public void process(HttpMethodEnum method, HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    String path = request.getRequestURI().substring(Constants.API_PATH_PREFIX.length() + 2);
    ApiAssignmentEntity apiConfigEntity = apiAssignmentDao.getByUk(method, path);
    if (null == apiConfigEntity || !apiConfigEntity.getStatus()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      String message = String.format("/%s/%s[%s]", Constants.API_PATH_PREFIX, path, method.name());
      ResultEntity result = ResultEntity.failed(ResponseErrorCode.ERROR_PATH_NOT_EXISTS, message);
      response.getWriter().append(JSONUtil.toJsonStr(result));
    } else {
      ResultEntity result = apiExecuteService.execute(apiConfigEntity, request, response);
      if (0 != result.getCode()) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      response.getWriter().append(JSONUtil.toJsonStr(result));
    }
  }

}
