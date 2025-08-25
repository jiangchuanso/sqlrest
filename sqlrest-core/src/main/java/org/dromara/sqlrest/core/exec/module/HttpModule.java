// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.exec.module;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.dromara.sqlrest.common.service.VarModuleInterface;
import org.dromara.sqlrest.core.exec.annotation.Comment;
import org.dromara.sqlrest.core.exec.annotation.Module;
import org.dromara.sqlrest.core.executor.AlarmHttpRequestFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@NoArgsConstructor
@Module(HttpModule.VAR_NAME)
public class HttpModule implements VarModuleInterface {

  protected static final String VAR_NAME = "http";
  protected static final RestTemplate template = new RestTemplate(new AlarmHttpRequestFactory());

  private HttpHeaders httpHeaders = new HttpHeaders();
  private Class<?> responseType = Object.class;
  private MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
  private MultiValueMap<String, Object> data = new LinkedMultiValueMap<>();
  private Map<String, ?> variables = new HashMap<>();
  private String url;
  private HttpMethod method = HttpMethod.GET;
  private HttpEntity<Object> entity = null;
  private Object requestBody;

  public HttpModule(String url) {
    this.url = url;
  }

  @Override
  public String getVarModuleName() {
    return VAR_NAME;
  }

  @Comment("创建连接")
  public HttpModule connect(@Comment("目标URL") String url) {
    return new HttpModule(url);
  }

  @Comment("设置URL参数")
  public HttpModule param(@Comment("参数名") String key, @Comment("参数值") Object... values) {
    if (values != null) {
      for (Object value : values) {
        this.params.add(key, value);
      }
    }
    return this;
  }

  @Comment("批量设置URL参数")
  public HttpModule param(@Comment("参数值") Map<String, Object> values) {
    values.forEach((key, value) -> param(key, Objects.toString(value, "")));
    return this;
  }

  @Comment("设置form参数")
  public HttpModule data(@Comment("参数名") String key, @Comment("参数值") Object... values) {
    if (values != null) {
      for (Object value : values) {
        this.data.add(key, value);
      }
    }
    return this;
  }

  @Comment("批量设置form参数")
  public HttpModule data(@Comment("参数值") Map<String, Object> values) {
    values.forEach((key, value) -> data(key, Objects.toString(value, "")));
    return this;
  }

  @Comment("设置header")
  public HttpModule header(@Comment("header键") String key, @Comment("header值") String value) {
    httpHeaders.add(key, value);
    return this;
  }

  @Comment("批量设置header")
  public HttpModule header(@Comment("header值") Map<String, Object> values) {
    values.entrySet()
        .stream()
        .filter(it -> it.getValue() != null)
        .forEach(entry -> header(entry.getKey(), entry.getValue().toString()));
    return this;
  }

  @Comment("设置请求方法，默认GET")
  public HttpModule method(@Comment("请求方法") HttpMethod method) {
    this.method = method;
    return this;
  }

  @Comment("设置`RequestBody`")
  public HttpModule body(@Comment("body值") Object requestBody) {
    this.requestBody = requestBody;
    this.contentType(MediaType.APPLICATION_JSON);
    return this;
  }

  @Comment("设置`ContentType`")
  public HttpModule contentType(@Comment("Content-Type值") String contentType) {
    return contentType(MediaType.parseMediaType(contentType));
  }

  @Comment("设置`ContentType`")
  public HttpModule contentType(@Comment("Content-Type值") MediaType mediaType) {
    this.httpHeaders.setContentType(mediaType);
    return this;
  }

  @Comment("设置返回值为`byte[]`")
  public HttpModule expectBytes() {
    this.responseType = byte[].class;
    return this;
  }

  @Comment("设置返回值为`String`")
  public HttpModule expectString() {
    this.responseType = String.class;
    return this;
  }

  @Comment("发送`GET`请求")
  public ResponseEntity<?> get() {
    this.method(HttpMethod.GET);
    return this.execute();
  }

  @Comment("发送`POST`请求")
  public ResponseEntity<?> post() {
    this.method(HttpMethod.POST);
    return this.execute();
  }

  @Comment("发送`PUT`请求")
  public ResponseEntity<?> put() {
    this.method(HttpMethod.PUT);
    return this.execute();
  }

  @Comment("发送`DELETE`请求")
  public ResponseEntity<?> delete() {
    this.method(HttpMethod.DELETE);
    return this.execute();
  }

  @Comment("执行请求")
  public ResponseEntity<?> execute() {
    if (!this.params.isEmpty()) {
      String queryString = this.params.entrySet().stream()
          .map(it -> it.getValue().stream()
              .map(value -> it.getKey() + "=" + value)
              .collect(Collectors.joining("&"))
          ).collect(Collectors.joining("&"));
      if (StringUtils.isNotBlank(queryString)) {
        this.url += (this.url.contains("?") ? "&" : "?") + queryString;
      }
    }
    if (!this.data.isEmpty()) {
      this.entity = new HttpEntity<>(this.data, this.httpHeaders);
    } else if (this.entity == null && this.requestBody != null) {
      this.entity = new HttpEntity<>(this.requestBody, this.httpHeaders);
    } else {
      this.entity = new HttpEntity<>(null, this.httpHeaders);
    }
    return template.exchange(url, this.method, entity, responseType, variables);
  }
}