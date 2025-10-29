// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.executor.model;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.dromara.sqlrest.common.dto.ResultEntity;
import org.dromara.sqlrest.core.exec.ApiAssignmentCache;
import org.dromara.sqlrest.core.exec.ApiExecuteService;
import org.dromara.sqlrest.core.util.JacksonUtils;
import org.dromara.sqlrest.persistence.entity.ApiAssignmentEntity;
import org.dromara.sqlrest.common.enums.DataTypeFormatEnum;

public class HttpApiServlet extends HttpServlet {

  private ApiExecuteService apiExecuteService;
  private boolean printSqlLog;

  public HttpApiServlet(ApiExecuteService apiExecuteService, boolean printSqlLog) {
    this.apiExecuteService = apiExecuteService;
    this.printSqlLog = printSqlLog;
  }

  private void process(HttpServletRequest request, HttpServletResponse response) throws IOException {
    ApiAssignmentEntity apiConfigEntity = ApiAssignmentCache.get();
    ResultEntity result = apiExecuteService.execute(apiConfigEntity, request, printSqlLog);
    
    // 从responseFormat配置中读取USE_SYSTEM_RESPONSE_FORMAT的值
    String useSystemResponseFormat = apiConfigEntity.getResponseFormat().get(DataTypeFormatEnum.USE_SYSTEM_RESPONSE_FORMAT);
    boolean useSystemFormat = !"false".equalsIgnoreCase(useSystemResponseFormat);
    
    String json;
    if (useSystemFormat) {
      // 返回完整的ResultEntity
      json = JacksonUtils.toJsonStr(result, apiConfigEntity.getResponseFormat());
    } else {
      // 只返回data部分
      json = JacksonUtils.toJsonStr(result.getData(), apiConfigEntity.getResponseFormat());
    }
    
    response.getWriter().append(json);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    process(req, resp);
  }

  @Override
  protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    process(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    process(req, resp);
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    process(req, resp);
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    process(req, resp);
  }
}

