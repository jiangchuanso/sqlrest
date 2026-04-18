// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.manager.service;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.sqlrest.common.consts.Constants;
import org.dromara.sqlrest.common.enums.NodeStatusEnum;
import org.dromara.sqlrest.common.exception.CommonException;
import org.dromara.sqlrest.common.exception.ResponseErrorCode;
import org.dromara.sqlrest.core.dto.TopologyNodeResponse;
import org.dromara.sqlrest.core.executor.AlarmHttpRequestFactory;
import org.dromara.sqlrest.manager.config.SqlrestUrlConfiguration;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class NodeService {

  private static final RestTemplate restTemplate = new RestTemplate(new AlarmHttpRequestFactory());

  @Resource
  private DiscoveryClient discoveryClient;
  @Resource
  private SqlrestUrlConfiguration sqlrestUrlConfiguration;

  public String getGatewayAddr() {
    List<ServiceInstance> instances = discoveryClient.getInstances(Constants.GATEWAY_APPLICATION_NAME);
    ServiceInstance instance = instances.stream().findAny().orElse(null);
    // 有且仅当服务确实存在时，才优先使用外部配置
    if (StringUtils.isNotBlank(sqlrestUrlConfiguration.getGateway()) && instance != null) {
      log.info("Configured Gateway Address found :{},Skip auto self discover", sqlrestUrlConfiguration.getGateway());
      return sqlrestUrlConfiguration.getGateway();
    }
    if (null != instance) {
      return String.format("http://%s:%d", instance.getHost(), instance.getPort());
    }
    throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_NOT_EXISTS, "gateway.not.found");
  }

  public String getApiPrefix() {
    List<ServiceInstance> instances = discoveryClient.getInstances(Constants.GATEWAY_APPLICATION_NAME);
    ServiceInstance instance = instances.stream().findAny().orElse(null);
    // 有且仅当服务确实存在时，才优先使用外部配置
    if (StringUtils.isNotBlank(sqlrestUrlConfiguration.getGateway()) && instance != null) {
      log.info("Configured Gateway Address found :{},Skip auto self discover", sqlrestUrlConfiguration.getGateway());
      return sqlrestUrlConfiguration.getGateway() + "/" + Constants.API_PATH_PREFIX + "/";
    }
    if (null != instance) {
      return String.format("http://%s:%d/%s/", instance.getHost(), instance.getPort(), Constants.API_PATH_PREFIX);
    }
    throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_NOT_EXISTS, "gateway.not.found");
  }

  public List<TopologyNodeResponse> getNodesTopology() {
    List<String> serviceIds = discoveryClient.getServices();
    if (CollectionUtils.isEmpty(serviceIds)) {
      return Collections.emptyList();
    }

    List<TopologyNodeResponse> nodes = new ArrayList<>();
    for (String serviceId : serviceIds) {
      List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
      for (ServiceInstance instance : instances) {
        NodeMetrics metrics = fetchNodeMetrics(instance);
        nodes.add(
            TopologyNodeResponse.builder()
                .serviceId(instance.getServiceId())
                .instanceId(instance.getInstanceId())
                .host(instance.getHost())
                .port(instance.getPort())
                .memory(metrics.getMemory())
                .cpu(metrics.getCpu())
                .disk(metrics.getDisk())
                .status(metrics.getStatus())
                .build());
      }
    }
    return nodes;
  }

  /**
   * 获取节点指标信息
   */
  private NodeMetrics fetchNodeMetrics(ServiceInstance instance) {
    String baseUrl = String.format("http://%s:%d", instance.getHost(), instance.getPort());
    try {
      return fetchMetricsFromActuator(baseUrl);
    } catch (Exception e) {
      log.warn("Failed to fetch metrics from actuator for {}: {}", baseUrl, e.getMessage());
      return fetchLocalMetrics();
    }
  }

  /**
   * 从 Actuator 端点获取指标
   */
  private NodeMetrics fetchMetricsFromActuator(String baseUrl) {
    NodeMetrics.NodeMetricsBuilder builder = NodeMetrics.builder();

    // 获取健康状态
    NodeStatusEnum status = checkHealth(baseUrl);
    builder.status(status);

    // 获取内存使用率
    try {
      Integer memoryUsage = fetchMemoryUsage(baseUrl);
      builder.memory(memoryUsage);
    } catch (Exception e) {
      log.debug("Failed to fetch memory metric: {}", e.getMessage());
      builder.memory(0);
    }

    // 获取CPU使用率
    try {
      Integer cpuUsage = fetchCpuUsage(baseUrl);
      builder.cpu(cpuUsage);
    } catch (Exception e) {
      log.debug("Failed to fetch cpu metric: {}", e.getMessage());
      builder.cpu(0);
    }

    // 获取磁盘使用率
    try {
      Integer diskUsage = fetchDiskUsage(baseUrl);
      builder.disk(diskUsage);
    } catch (Exception e) {
      log.debug("Failed to fetch disk metric: {}", e.getMessage());
      builder.disk(0);
    }

    return builder.build();
  }

  /**
   * 检查健康状态
   */
  private NodeStatusEnum checkHealth(String baseUrl) {
    try {
      ResponseEntity<Map> response = restTemplate.getForEntity(
          baseUrl + "/actuator/health", Map.class);
      if (response.getBody() != null) {
        String healthStatus = (String) response.getBody().get("status");
        if ("UP".equalsIgnoreCase(healthStatus)) {
          return NodeStatusEnum.normal;
        }
      }
      return NodeStatusEnum.warning;
    } catch (Exception e) {
      log.warn("Health check failed for {}: {}", baseUrl, e.getMessage());
      return NodeStatusEnum.warning;
    }
  }

  /**
   * 获取内存使用率
   */
  private Integer fetchMemoryUsage(String baseUrl) {
    Long used = fetchMetricValue(baseUrl, "jvm.memory.used");
    Long max = fetchMetricValue(baseUrl, "jvm.memory.max");
    if (used != null && max != null && max > 0) {
      return (int) ((used * 100) / max);
    }
    return 0;
  }

  /**
   * 获取CPU使用率
   */
  private Integer fetchCpuUsage(String baseUrl) {
    Double cpuUsage = fetchMetricValueDouble(baseUrl, "system.cpu.usage");
    if (cpuUsage != null) {
      return (int) (cpuUsage * 100);
    }
    return 0;
  }

  /**
   * 获取磁盘使用率
   */
  private Integer fetchDiskUsage(String baseUrl) {
    Long free = fetchMetricValue(baseUrl, "disk.free");
    Long total = fetchMetricValue(baseUrl, "disk.total");
    if (free != null && total != null && total > 0) {
      return (int) (((total - free) * 100) / total);
    }
    return 0;
  }

  /**
   * 从 Actuator metrics 端点获取长整型指标值
   */
  @SuppressWarnings("unchecked")
  private Long fetchMetricValue(String baseUrl, String metricName) {
    try {
      ResponseEntity<Map> response = restTemplate.getForEntity(
          baseUrl + "/actuator/metrics/" + metricName, Map.class);
      if (response.getBody() != null) {
        List<Map<String, Object>> measurements = (List<Map<String, Object>>) response.getBody().get("measurements");
        if (measurements != null && !measurements.isEmpty()) {
          Object value = measurements.get(0).get("value");
          if (value instanceof Number) {
            return ((Number) value).longValue();
          }
        }
      }
    } catch (Exception e) {
      log.debug("Failed to fetch metric {}: {}", metricName, e.getMessage());
    }
    return null;
  }

  /**
   * 从 Actuator metrics 端点获取双精度浮点型指标值
   */
  @SuppressWarnings("unchecked")
  private Double fetchMetricValueDouble(String baseUrl, String metricName) {
    try {
      ResponseEntity<Map> response = restTemplate.getForEntity(
          baseUrl + "/actuator/metrics/" + metricName, Map.class);
      if (response.getBody() != null) {
        List<Map<String, Object>> measurements = (List<Map<String, Object>>) response.getBody().get("measurements");
        if (measurements != null && !measurements.isEmpty()) {
          Object value = measurements.get(0).get("value");
          if (value instanceof Number) {
            return ((Number) value).doubleValue();
          }
        }
      }
    } catch (Exception e) {
      log.debug("Failed to fetch metric {}: {}", metricName, e.getMessage());
    }
    return null;
  }

  /**
   * 获取本地指标（作为 fallback）
   */
  private NodeMetrics fetchLocalMetrics() {
    Runtime runtime = Runtime.getRuntime();
    long totalMemory = runtime.totalMemory();
    long freeMemory = runtime.freeMemory();
    long usedMemory = totalMemory - freeMemory;
    long maxMemory = runtime.maxMemory();

    int memoryUsage = maxMemory > 0 ? (int) ((usedMemory * 100) / maxMemory) : 0;

    OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    int cpuUsage = 0;
    if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
      cpuUsage = (int) (((com.sun.management.OperatingSystemMXBean) osBean).getSystemCpuLoad() * 100);
    }

    java.io.File file = new java.io.File(".");
    long totalSpace = file.getTotalSpace();
    long freeSpace = file.getFreeSpace();
    int diskUsage = totalSpace > 0 ? (int) (((totalSpace - freeSpace) * 100) / totalSpace) : 0;

    NodeStatusEnum status = (memoryUsage > 90 || cpuUsage > 90) ? NodeStatusEnum.warning : NodeStatusEnum.normal;

    return NodeMetrics.builder()
        .memory(memoryUsage)
        .cpu(cpuUsage)
        .disk(diskUsage)
        .status(status)
        .build();
  }

  /**
   * 节点指标内部类
   */
  @Data
  @Builder
  private static class NodeMetrics {
    private Integer memory;
    private Integer cpu;
    private Integer disk;
    private NodeStatusEnum status;
  }
}
