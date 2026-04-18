// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.gateway.filter;

import cn.hutool.json.JSONUtil;
import java.util.Locale;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.sqlrest.common.consts.Constants;
import org.dromara.sqlrest.common.dto.ResultEntity;
import org.dromara.sqlrest.common.exception.ResponseErrorCode;
import org.dromara.sqlrest.common.util.I18nUtils;
import org.dromara.sqlrest.common.util.InetUtils;
import org.dromara.sqlrest.core.gateway.FirewallFilterService;
import org.dromara.sqlrest.core.util.LocaleUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ClientAddressFilter implements GlobalFilter, Ordered {

  @Resource
  private FirewallFilterService firewallFilterService;

  /*每30秒执行一次*/
  @EventListener(ApplicationReadyEvent.class)
  @Scheduled(cron = "${cron.firewall.expression:0/30 0 * * * ?}")
  public void loadFlowRules() {
    try {
      firewallFilterService.refresh();
    } catch (Exception e) {
      log.error("load firewall rules failed:{}", e.getMessage(), e);
    }
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    ServerHttpResponse response = exchange.getResponse();
    String path = request.getURI().getPath();
    String method = request.getMethod().name();
    String clientHostAddr = request.getRemoteAddress().getHostString();
    if (!firewallFilterService.canAccess(clientHostAddr)) {
      log.info("Forbidden access for client : {}, path : {}, method : {}", clientHostAddr, path, method);
      Locale locale = LocaleUtils.resolveLocale(request);
      String message = I18nUtils.getMessage("ERROR_CLIENT_FORBIDDEN", locale) + ", " + clientHostAddr;
      ResultEntity data = ResultEntity.failed(ResponseErrorCode.ERROR_CLIENT_FORBIDDEN, message);
      String json = JSONUtil.toJsonStr(data);
      DataBuffer wrap = response.bufferFactory().wrap(json.getBytes());
      return response.writeWith(Mono.just(wrap));
    } else {
      log.info("access api from client : {}, path : {}, method : {}", clientHostAddr, path, method);
    }

    ServerHttpRequest newRequest = request.mutate()
        .header(Constants.REQUEST_HEADER_GATEWAY_IP, InetUtils.getLocalIpStr())
        .build();
    return chain.filter(exchange.mutate().request(newRequest).build());
  }

  @Override
  public int getOrder() {
    return 0;
  }


}
