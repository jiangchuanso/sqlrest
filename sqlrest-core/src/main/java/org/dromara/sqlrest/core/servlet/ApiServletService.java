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

import org.dromara.sqlrest.common.consts.Constants;
import org.dromara.sqlrest.common.dto.ResultEntity;
import org.dromara.sqlrest.common.enums.HttpMethodEnum;
import org.dromara.sqlrest.core.exec.ApiExecuteService;
import org.dromara.sqlrest.core.util.JacksonUtils;
import org.dromara.sqlrest.persistence.dao.ApiAssignmentDao;
import org.dromara.sqlrest.persistence.entity.ApiAssignmentEntity;
import java.io.IOException;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
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
    String path = request.getRequestURI().substring(Constants.API_PATH_PREFIX.length() + 2);
    ApiAssignmentEntity apiConfigEntity = apiAssignmentDao.getByUk(method, path);
    ResultEntity result = apiExecuteService.execute(apiConfigEntity, request);
    String json = JacksonUtils.toJsonStr(result, apiConfigEntity.getResponseFormat());
    response.getWriter().append(json);
  }

}
