// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.sqlrest.common.enums;

import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import cn.hutool.json.JSONUtil;
import lombok.Getter;

@Getter
public enum ParamTypeEnum {
  LONG("整型", "number", 0L, Long.class, (String str) -> StringUtils.isNotBlank(str) ? Long.valueOf(str) : null),
  DOUBLE("浮点型", "number", 0D, Double.class, (String str) -> StringUtils.isNotBlank(str) ? Double.valueOf(str) : null),
  STRING("字符串", "string", "", String.class, (String str) -> str),
  DATE("日期", "string", "", String.class, (String str) -> str),
  TIME("时间", "string", "", String.class, (String str) -> str),
  //fix: 直接在swagger上展示boolean类型
  BOOLEAN("布尔", "boolean", "true", Boolean.class, (String str) -> StringUtils.isNotBlank(str) ? Boolean.parseBoolean(str): null),
  //fix: 修复对象不传时，默认值为空字符串问题
  OBJECT("对象", "object", "{}", Map.class, (String str) -> StringUtils.isNotBlank(str) ? JSONUtil.toBean(str, Map.class) : null);

  private String name;
  private String jsType;
  private Object example;
  private Class clazz;
  private Function<String, Object> converter;

  ParamTypeEnum(String name, String jsType, Object example, Class clazz, Function<String, Object> converter) {
    this.name = name;
    this.jsType = jsType;
    this.example = example;
    this.clazz = clazz;
    this.converter = converter;
  }

  public boolean isObject() {
    return OBJECT == this;
  }
}
