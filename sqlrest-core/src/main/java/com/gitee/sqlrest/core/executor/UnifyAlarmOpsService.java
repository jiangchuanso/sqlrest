// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.sqlrest.core.executor;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.gitee.sqlrest.common.enums.OnOffEnum;
import com.gitee.sqlrest.core.dto.NameValueBaseResponse;
import com.gitee.sqlrest.core.dto.TestAlarmConfigRequest;
import com.gitee.sqlrest.core.dto.UpdateAlarmConfigRequest;
import com.gitee.sqlrest.persistence.dao.UnifyAlarmDao;
import com.gitee.sqlrest.persistence.entity.UnifyAlarmEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.text.StrSubstitutor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class UnifyAlarmOpsService {

  @Resource
  private UnifyAlarmDao unifyAlarmDao;
  private final RestTemplate restTemplate;

  public UnifyAlarmOpsService() {
    this.restTemplate = new RestTemplate(new AlarmHttpRequestFactory());
  }

  public UnifyAlarmEntity getUnifyAlarmConfig() {
    return unifyAlarmDao.getUnifyAlarmConfig();
  }

  public List<NameValueBaseResponse> getExampleDataModel() {
    Map<String, String> dataModel = new HashMap<>(8);
    dataModel.put("path", "/api/test/create");
    dataModel.put("method", "POST");
    dataModel.put("contentType", "application/json");
    dataModel.put("name", "test interface for create");
    dataModel.put("description", "this is description!");
    dataModel.put("open", "false");
    dataModel.put("clientKey", "test");
    dataModel.put("ipAddr", "127.0.0.1");
    dataModel.put("userAgent", "sqlrest");
    dataModel.put("exception", "this is test alarm message");

    List<NameValueBaseResponse> lists = new ArrayList<>();
    dataModel.forEach((k, v) -> lists.add(NameValueBaseResponse.builder().key(k).value(v).build()));
    return lists;
  }

  public void updateUnifyAlarmConfig(UpdateAlarmConfigRequest request) {
    unifyAlarmDao.update(
        request.getStatus(),
        request.getEndpoint(),
        request.getContentType(),
        request.getInputTemplate());
  }

  public void testUnifyAlarmConfig(TestAlarmConfigRequest request) {
    UnifyAlarmEntity config = UnifyAlarmEntity.builder()
        .status(OnOffEnum.ON)
        .endpoint(request.getEndpoint())
        .contentType(request.getContentType())
        .inputTemplate(request.getInputTemplate())
        .build();
    Map<String, String> dataModel = Optional.ofNullable(request.getDataModel())
        .orElseGet(ArrayList::new).stream()
        .collect(
            Collectors.toMap(
                NameValueBaseResponse::getKey,
                NameValueBaseResponse::getValue,
                (a, b) -> b));
    dataModel.put("accessTime", DateUtil.now());
    handleAlarm(config, dataModel);
  }

  public void triggerAlarm(Map<String, String> dataModel) {
    UnifyAlarmEntity config = unifyAlarmDao.getUnifyAlarmConfig();
    if (OnOffEnum.OFF == config.getStatus()) {
      return;
    }

    try {
      handleAlarm(config, dataModel);
    } catch (Exception e) {
      log.error("Send alarm message failed,{}", e.getMessage(), e);
    }
  }

  private ResponseEntity<String> handleAlarm(UnifyAlarmEntity config, Map<String, String> dataModel) {
    StrSubstitutor strSubstitutor = new StrSubstitutor(escape(dataModel));
    String bodyStr = strSubstitutor.replace(config.getInputTemplate());
    if (log.isDebugEnabled()) {
      log.debug("Send alarm message body:\n{}", bodyStr);
    }
    ResponseEntity<String> ret = sentAlarm(config, bodyStr);
    if (ret.getStatusCodeValue() != HttpStatus.OK.value()) {
      log.warn("Error when send alarm message http status: {} ,body: {}", ret.getStatusCode(), ret.getBody());
    } else {
      log.info("Send alarm message http status: {} ,body: {}", ret.getStatusCode(), ret.getBody());
    }
    return ret;
  }

  private ResponseEntity<String> sentAlarm(UnifyAlarmEntity config, String bodyStr) {
    HttpHeaders headers = new HttpHeaders();
    MediaType type = MediaType.parseMediaType(config.getContentType().replace(";", "") + "; charset=UTF-8");
    headers.setContentType(type);
    headers.add("Accept", MediaType.ALL_VALUE);
    HttpEntity<String> httpEntity = new HttpEntity<>(bodyStr, headers);
    return restTemplate.exchange(config.getEndpoint(), HttpMethod.POST, httpEntity, String.class);
  }

  private Map<String, String> escape(Map<String, String> dataModel) {
    Map<String, String> newDataModel = new HashMap<>(dataModel.size());
    for (Map.Entry<String, String> entry : dataModel.entrySet()) {
      newDataModel.put(entry.getKey(), JSONUtil.escape(entry.getValue()));
    }
    return newDataModel;
  }
}
