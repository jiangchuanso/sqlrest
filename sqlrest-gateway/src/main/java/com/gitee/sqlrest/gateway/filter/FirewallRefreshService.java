package com.gitee.sqlrest.gateway.filter;

import com.gitee.sqlrest.core.gateway.FirewallFilterService;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FirewallRefreshService {

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
}
