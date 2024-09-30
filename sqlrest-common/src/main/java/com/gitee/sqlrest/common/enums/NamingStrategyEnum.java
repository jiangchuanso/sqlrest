package com.gitee.sqlrest.common.enums;

public enum NamingStrategyEnum {
  LOWER_CAMEL_CASE("属性名转换为小驼峰命名"),
  UPPER_CAMEL_CASE("属性名转换为大驼峰命名"),
  SNAKE_CASE("属性名转换为蛇形命名"),
  LOWER_CASE("属性名转换为小写字母"),
  ;
  private String description;

  NamingStrategyEnum(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
