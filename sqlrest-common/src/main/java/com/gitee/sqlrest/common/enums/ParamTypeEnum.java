package com.gitee.sqlrest.common.enums;

import java.util.function.Function;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public enum ParamTypeEnum {
  LONG("整型", "number", (String str) -> StringUtils.isNotBlank(str) ? Long.valueOf(str) : str),
  DOUBLE("浮点型", "number", (String str) -> StringUtils.isNotBlank(str) ? Double.valueOf(str) : str),
  STRING("字符串", "string", (String str) -> str),
  DATE("日期", "string", (String str) -> str),
  TIME("时间", "string", (String str) -> str),
  ;

  private String name;
  private String jsType;
  private Function<String, Object> converter;

  ParamTypeEnum(String name, String jsType, Function<String, Object> converter) {
    this.name = name;
    this.jsType = jsType;
    this.converter = converter;
  }
}
