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

import cn.hutool.extra.spring.SpringUtil;
import org.dromara.sqlrest.common.consts.Constants;
import org.dromara.sqlrest.common.dto.PageResult;
import org.dromara.sqlrest.common.exception.CommonException;
import org.dromara.sqlrest.common.exception.ResponseErrorCode;
import org.dromara.sqlrest.common.util.TokenUtils;
import org.dromara.sqlrest.core.dto.EntitySearchRequest;
import org.dromara.sqlrest.core.dto.McpToolResponse;
import org.dromara.sqlrest.core.dto.McpToolSaveRequest;
import org.dromara.sqlrest.core.util.ApiPathUtils;
import org.dromara.sqlrest.manager.model.McpToolCallHandler;
import org.dromara.sqlrest.persistence.dao.ApiAssignmentDao;
import org.dromara.sqlrest.persistence.dao.ApiModuleDao;
import org.dromara.sqlrest.persistence.dao.McpClientDao;
import org.dromara.sqlrest.persistence.dao.McpToolDao;
import org.dromara.sqlrest.persistence.entity.ApiAssignmentEntity;
import org.dromara.sqlrest.persistence.entity.ApiModuleEntity;
import org.dromara.sqlrest.persistence.entity.McpClientEntity;
import org.dromara.sqlrest.persistence.entity.McpToolEntity;
import org.dromara.sqlrest.persistence.util.PageUtils;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class McpManageService {

  @Resource
  private McpClientDao mcpClientDao;
  @Resource
  private McpToolDao mcpToolDao;
  @Resource
  private ApiModuleDao apiModuleDao;
  @Resource
  private ApiAssignmentDao apiAssignmentDao;
  @Resource
  private McpSyncServer mcpSyncServer;

  @EventListener(ApplicationReadyEvent.class)
  public void loadMcpTools() {
    try {
      List<McpToolEntity> lists = mcpToolDao.listAll(null);
      lists.forEach(this::addMcpTool);
      log.info("Finish load total count [{}] mcp tools to memory.", lists.size());
    } catch (Exception e) {
      log.error("Failed load mcp tools to memory: {}", e.getMessage(), e);
      throw e;
    }
  }

  public String getMcpSseEndpoint() {
    DiscoveryClient discoveryClient = SpringUtil.getBean(DiscoveryClient.class);
    List<ServiceInstance> instances = discoveryClient.getInstances(Constants.MANAGER_APPLICATION_NAME);
    ServiceInstance instance = instances.stream().findAny().orElse(null);
    return String.format("http://%s:%d%s?%s=", instance.getHost(), instance.getPort(),
        Constants.DEFAULT_SSE_ENDPOINT, Constants.DEFAULT_SSE_TOKEN_PRAM_NAME);
  }

  public void createClient(String name) {
    try {
      mcpClientDao.insert(
          McpClientEntity.builder()
              .name(name)
              .token(TokenUtils.generateValue())
              .build()
      );
    } catch (DuplicateKeyException e) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_ALREADY_EXISTS, "client name already exists");
    }
  }

  public void updateClient(Long id, String newName) {
    McpClientEntity clientEntity = mcpClientDao.getById(id);
    clientEntity.setName(newName);
    try {
      mcpClientDao.updateById(clientEntity);
    } catch (DuplicateKeyException e) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_ALREADY_EXISTS, "module name already exists");
    }
  }

  public void deleteClient(Long id) {
    mcpClientDao.deleteById(id);
  }

  public PageResult<McpClientEntity> listClientAll(EntitySearchRequest request) {
    return PageUtils.getPage(
        () -> mcpClientDao.listAll(request.getSearchText()),
        request.getPage(),
        request.getSize()
    );
  }

  @Transactional(rollbackFor = Exception.class)
  public void createTool(McpToolSaveRequest request) {
    if (null == request.getApiId()) {
      throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT, "apiId");
    }
    if (null == apiAssignmentDao.getById(request.getApiId(), false)) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_ALREADY_EXISTS,
          "apiId not exists,id=" + request.getApiId());
    }

    McpToolEntity toolEntity = McpToolEntity.builder()
        .name(request.getName())
        .description(request.getDescription())
        .apiId(request.getApiId())
        .build();
    try {
      mcpToolDao.insert(toolEntity);
      addMcpTool(toolEntity);
    } catch (DuplicateKeyException e) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_ALREADY_EXISTS, "tool name already exists");
    }
  }

  @Transactional(rollbackFor = Exception.class)
  public void updateTool(McpToolSaveRequest request) {
    if (null == request.getId()) {
      throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT, "id");
    }
    if (null == request.getApiId()) {
      throw new CommonException(ResponseErrorCode.ERROR_INVALID_ARGUMENT, "apiId");
    }

    McpToolEntity exists = mcpToolDao.getById(request.getId());
    if (null == exists) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_NOT_EXISTS, "id=" + request.getId());
    }
    if (null == apiAssignmentDao.getById(request.getApiId(), false)) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_ALREADY_EXISTS,
          "apiId=" + request.getApiId());
    }
    McpToolEntity newToolEntity = McpToolEntity.builder()
        .id(request.getId())
        .name(request.getName())
        .description(request.getDescription())
        .apiId(request.getApiId())
        .build();
    try {
      mcpToolDao.updateById(newToolEntity);
      updateMcpTool(exists.getName(), newToolEntity);
    } catch (DuplicateKeyException e) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_ALREADY_EXISTS, "tool name already exists");
    }
  }

  @Transactional(rollbackFor = Exception.class)
  public void deleteTool(Long id) {
    McpToolEntity toolEntity = mcpToolDao.getById(id);
    if (null == toolEntity) {
      throw new CommonException(ResponseErrorCode.ERROR_RESOURCE_NOT_EXISTS, "id=" + id);
    }

    mcpToolDao.deleteById(id);
    deleteMcpTool(toolEntity.getName());
  }

  public PageResult<McpToolResponse> listToolAll(EntitySearchRequest request) {
    PageResult<McpToolEntity> pageResult = PageUtils.getPage(
        () -> mcpToolDao.listAll(request.getSearchText()),
        request.getPage(),
        request.getSize());
    List<McpToolResponse> responseList = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(pageResult.getData())) {
      List<Long> apiIdList = pageResult.getData().stream().map(McpToolEntity::getApiId)
          .collect(Collectors.toList());
      Map<Long, ApiAssignmentEntity> apiConfigIdMap = apiAssignmentDao.getByIds(apiIdList)
          .stream().collect(Collectors.toMap(ApiAssignmentEntity::getId, Function.identity()));
      Map<Long, String> apiModuleIdNameMap = apiModuleDao.listAll(null)
          .stream().collect(Collectors.toMap(ApiModuleEntity::getId, ApiModuleEntity::getName));
      for (McpToolEntity toolEntity : pageResult.getData()) {
        ApiAssignmentEntity config = apiConfigIdMap.get(toolEntity.getApiId());
        McpToolResponse response = McpToolResponse.builder()
            .id(toolEntity.getId())
            .name(toolEntity.getName())
            .description(toolEntity.getDescription())
            .moduleId(config.getModuleId())
            .moduleName(apiModuleIdNameMap.get(config.getModuleId()))
            .apiId(toolEntity.getApiId())
            .apiName(config.getName())
            .apiMethod(config.getMethod().name())
            .apiPath(ApiPathUtils.getFullPath(config.getPath()))
            .createTime(toolEntity.getCreateTime())
            .updateTime(toolEntity.getUpdateTime())
            .build();
        responseList.add(response);
      }
    }
    PageResult<McpToolResponse> answer = new PageResult<>();
    answer.setPagination(pageResult.getPagination());
    answer.setData(responseList);
    return answer;
  }

  private void addMcpTool(McpToolEntity toolEntity) {
    ApiAssignmentEntity config = apiAssignmentDao.getById(toolEntity.getApiId(), true);
    McpToolCallHandler toolCallHandler = new McpToolCallHandler(
        toolEntity.getName(),
        toolEntity.getDescription(),
        config);
    mcpSyncServer.addTool(
        new McpServerFeatures.SyncToolSpecification(
            toolCallHandler.getMcpToolSchema(),
            toolCallHandler::executeTool
        )
    );
  }

  private void updateMcpTool(String oldToolName, McpToolEntity toolEntity) {
    mcpSyncServer.removeTool(oldToolName);
    addMcpTool(toolEntity);
  }

  private void deleteMcpTool(String toolName) {
    mcpSyncServer.removeTool(toolName);
  }
}
