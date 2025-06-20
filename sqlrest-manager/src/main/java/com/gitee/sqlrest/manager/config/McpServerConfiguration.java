// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.sqlrest.manager.config;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitee.sqlrest.common.consts.Constants;
import com.gitee.sqlrest.common.util.PomVersionUtils;
import com.gitee.sqlrest.persistence.dao.McpClientDao;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.WebMvcSseServerAuthChecker;
import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
@EnableWebMvc
public class McpServerConfiguration {

  @Bean
  public WebMvcSseServerAuthChecker serverAuthChecker() {
    return new WebMvcSseServerAuthChecker() {

      @Override
      public String getTokenParamName() {
        return Constants.DEFAULT_SSE_TOKEN_PRAM_NAME;
      }

      @Override
      public boolean checkTokenValid(String token) {
        McpClientDao clientDao = SpringUtil.getBean(McpClientDao.class);
        return clientDao.existsAccessToken(token);
      }
    };
  }

  @Bean
  public WebMvcSseServerTransportProvider webMvcSseServerTransportProvider(WebMvcSseServerAuthChecker checker) {
    return new WebMvcSseServerTransportProvider(new ObjectMapper(), checker, Constants.MESSAGE_ENDPOINT,
        Constants.DEFAULT_SSE_ENDPOINT);
  }

  @Bean
  public RouterFunction<ServerResponse> routerFunction(WebMvcSseServerTransportProvider transportProvider) {
    return transportProvider.getRouterFunction();
  }

  @Bean
  public McpSyncServer mcpSyncServer(WebMvcSseServerTransportProvider transportProvider) {
    McpSyncServer syncServer = McpServer.sync(transportProvider)
        .serverInfo(Constants.MCP_SERVER_NAME, PomVersionUtils.getProjectVersion())
        .capabilities(
            ServerCapabilities.builder()
                .resources(true, true)
                .tools(true)
                .prompts(true)
                .logging()
                .build())
        .build();
    syncServer.loggingNotification(
        LoggingMessageNotification.builder()
            .level(LoggingLevel.INFO)
            .logger("custom-logger")
            .data("Server initialized")
            .build());
    return syncServer;
  }

}
