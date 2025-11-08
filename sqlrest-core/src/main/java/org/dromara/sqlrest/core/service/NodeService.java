// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.sqlrest.common.consts.Constants;
import org.dromara.sqlrest.common.exception.CommonException;
import org.dromara.sqlrest.common.exception.ResponseErrorCode;
import org.dromara.sqlrest.core.configuration.SqlrestUrlConfiguration;
import org.dromara.sqlrest.core.dto.TopologyNodeResponse;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class NodeService {

  @Resource
  private DiscoveryClient discoveryClient;
  @Resource
  private SqlrestUrlConfiguration sqlrestUrlConfiguration;

  public String getGatewayAddr() {
    List<ServiceInstance> instances = discoveryClient.getInstances(Constants.GATEWAY_APPLICATION_NAME);
    ServiceInstance instance = instances.stream().findAny().orElse(null);
    // 有且仅当服务确实存在时，才优先使用外部配置
    if (StringUtils.isNotBlank(sqlrestUrlConfiguration.getGateway()) && instance != null) {
      log.info("使用外部配置的网关地址: {},跳过使用服务发现的网关地址",sqlrestUrlConfiguration.getGateway());
      return sqlrestUrlConfiguration.getGateway();
    }
    if (null != instance) {
      return String.format("http://%s:%d", instance.getHost(), instance.getPort());
    }
    throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_NOT_EXISTS, "No Gateway is founded");
  }

  public String getApiPrefix() {
    List<ServiceInstance> instances = discoveryClient.getInstances(Constants.GATEWAY_APPLICATION_NAME);
      ServiceInstance instance = instances.stream().findAny().orElse(null);
      // 有且仅当服务确实存在时，才优先使用外部配置
      if (StringUtils.isNotBlank(sqlrestUrlConfiguration.getGateway()) && instance != null) {
        log.info("使用外部配置的网关地址: {},跳过使用服务发现的网关地址",sqlrestUrlConfiguration.getGateway());
        return sqlrestUrlConfiguration.getGateway()+"/"+Constants.API_PATH_PREFIX+"/";
      }
      if (null != instance) {
      return String.format("http://%s:%d/%s/", instance.getHost(), instance.getPort(), Constants.API_PATH_PREFIX);
    }
    throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_NOT_EXISTS, "No Gateway is founded");
  }

  public List<TopologyNodeResponse> getNodesTopology() {
    List<TopologyNodeResponse> nodes = new ArrayList<>();
    List<String> serviceIds = discoveryClient.getServices();
    if (CollectionUtils.isEmpty(serviceIds)) {
      return nodes;
    }
    for (String serviceId : serviceIds) {
      List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
      for (ServiceInstance instance : instances) {
        nodes.add(
            TopologyNodeResponse.builder()
                .serviceId(instance.getServiceId())
                .instanceId(instance.getInstanceId())
                .host(instance.getHost())
                .port(instance.getPort())
                .build()
        );
      }
    }
    return nodes;
  }
}
