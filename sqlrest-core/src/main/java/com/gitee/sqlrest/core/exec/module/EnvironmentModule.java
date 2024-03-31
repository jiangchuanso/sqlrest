package com.gitee.sqlrest.core.exec.module;

import com.gitee.sqlrest.script.annotation.Comment;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class EnvironmentModule {

  private final Environment environment;

  public EnvironmentModule(Environment environment) {
    this.environment = environment;
  }

  @Comment("获取配置")
  public String get(@Comment(name = "key", value = "配置项") String key) {
    return environment.getProperty(key);
  }

  @Comment("获取配置")
  public String get(@Comment(name = "key", value = "配置项") String key,
      @Comment(name = "defaultValue", value = "未配置时的默认值") String defaultValue) {
    return environment.getProperty(key, defaultValue);
  }

}
