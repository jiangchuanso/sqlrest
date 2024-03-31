package com.gitee.sqlrest.cache;

import com.gitee.sqlrest.common.consts.Constants;
import com.hazelcast.config.Config;
import com.hazelcast.eureka.one.EurekaOneDiscoveryStrategyFactory;
import com.netflix.discovery.EurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastCacheConfig {

  @Bean
  public Config hazelcastCacheConfigFromEureka(EurekaClient eurekaClient) {
    EurekaOneDiscoveryStrategyFactory.setEurekaClient(eurekaClient);
    Config config = new Config();
    config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
    config.getNetworkConfig().getJoin().getEurekaConfig()
        .setEnabled(true)
        .setProperty("self-registration", "true")
        .setProperty("namespace", "hazelcast")
        .setProperty("use-metadata-for-host-and-port", "true")
        .setProperty("skip-eureka-registration-verification", "true");
    config.getMapConfig(Constants.CACHE_KEY_TOKEN_CLIENT)
        .setTimeToLiveSeconds(Constants.CLIENT_TOKEN_DURATION_SECONDS.intValue());
    return config;
  }

}
