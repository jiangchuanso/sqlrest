package org.dromara.sqlrest.manager.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 用于外部配置化，配置网关，管理服务地址的，外部一般都会有 nginx 或者其他反代服务存在
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "sqlrest.url")
public class SqlrestUrlConfiguration {

  private String gateway = "";
  private String manager = "";
}
