// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.sqlrest.core.servlet;

import com.gitee.sqlrest.cache.CacheFactory;
import com.gitee.sqlrest.common.consts.Constants;
import com.gitee.sqlrest.common.dto.AccessToken;
import com.gitee.sqlrest.common.enums.AliveTimeEnum;
import com.gitee.sqlrest.common.enums.DurationTimeEnum;
import com.gitee.sqlrest.common.exception.CommonException;
import com.gitee.sqlrest.common.exception.ResponseErrorCode;
import com.gitee.sqlrest.common.util.TokenUtils;
import com.gitee.sqlrest.persistence.dao.AppClientDao;
import com.gitee.sqlrest.persistence.entity.AppClientEntity;
import com.gitee.sqlrest.persistence.util.JsonUtils;
import java.util.Map;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ClientTokenService {

  @Resource
  private AppClientDao appClientDao;
  @Resource
  private CacheFactory cacheFactory;

  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    long currentTimestamp = getCurrentTimestamp();
    try {
      for (AppClientEntity appClient : appClientDao.listAll(null)) {
        appClient.setAppSecret("******");
        if (StringUtils.isNotBlank(appClient.getAccessToken())) {
          AccessToken clientToken = AccessToken.builder()
              .realName(appClient.getName())
              .appKey(appClient.getAppKey())
              .accessToken(appClient.getAccessToken())
              .createTimestamp(appClient.getUpdateTime().getTime() / 1000)
              .expireSeconds(appClient.getTokenAlive().getValue())
              .build();

          if (appClient.getExpireAt() > 0) {
            long expireSeconds = appClient.getExpireAt() - currentTimestamp;
            if (expireSeconds <= 0) {
              // 已经过期的无需再加载到缓存中了
              continue;
            }
            if (expireSeconds >= appClient.getTokenAlive().getValue()) {
              clientToken.setExpireSeconds(appClient.getTokenAlive().getValue());
            } else {
              clientToken.setExpireSeconds(expireSeconds);
            }
          }

          // TODO: 暂时也将一次性token也加载进缓存中，缺少判断是否过期的逻辑
          log.info("Load client app token from persistence :{}", JsonUtils.toJsonString(appClient));

          Map<String, AccessToken> tokenClientMap = cacheFactory
              .getCacheMap(Constants.CACHE_KEY_TOKEN_CLIENT, AccessToken.class);
          tokenClientMap.put(appClient.getAccessToken(), clientToken);
        }
      }
      log.info("Finish load client app token from persistence.");
    } catch (Exception e) {
      log.error("load client app token failed:{}", e.getMessage(), e);
      throw e;
    }
  }

  @Transactional(rollbackFor = Exception.class)
  public AccessToken generateToken(String clientId, String clientSecret) {
    AppClientEntity appClient = appClientDao.getByAppKey(clientId);
    if (null == appClient) {
      throw new CommonException(ResponseErrorCode.ERROR_CLIENT_FORBIDDEN, "clientId invalid");
    }
    if (!StringUtils.equals(appClient.getAppSecret(), clientSecret)) {
      throw new CommonException(ResponseErrorCode.ERROR_CLIENT_FORBIDDEN, "secret invalid");
    }
    if (DurationTimeEnum.TIME_VALUE == appClient.getExpireDuration()) {
      Boolean isExpired = getCurrentTimestamp() > appClient.getExpireAt();
      if (isExpired) {
        throw new CommonException(ResponseErrorCode.ERROR_CLIENT_FORBIDDEN, "clientId is expired");
      }
    } else if (DurationTimeEnum.ONLY_ONCE == appClient.getExpireDuration()) {
      if (!appClient.getCreateTime().equals(appClient.getUpdateTime())) {
        throw new CommonException(ResponseErrorCode.ERROR_CLIENT_FORBIDDEN, "clientId is expired");
      }
    }

    String token = TokenUtils.generateValue();
    Long createTimestamp = getCurrentTimestamp();
    if (AliveTimeEnum.LONGEVITY.equals(appClient.getTokenAlive())
        && StringUtils.isNotBlank(appClient.getAccessToken())) {
      token = appClient.getAccessToken();
      createTimestamp = appClient.getUpdateTime().getTime();
    }

    AccessToken clientToken = AccessToken.builder()
        .realName(appClient.getName())
        .appKey(clientId)
        .accessToken(token)
        .createTimestamp(createTimestamp)
        .build();
    if (appClient.getExpireAt() > 0) {
      long expireSeconds = appClient.getExpireAt() - getCurrentTimestamp();
      if (expireSeconds <= 0) {
        throw new CommonException(ResponseErrorCode.ERROR_CLIENT_FORBIDDEN, "app key is expired");
      }
      if (AliveTimeEnum.LONGEVITY.equals(appClient.getTokenAlive())) {
        clientToken.setExpireSeconds(appClient.getExpireAt() - createTimestamp);
      } else {
        if (expireSeconds > appClient.getTokenAlive().getValue()) {
          clientToken.setExpireSeconds(appClient.getTokenAlive().getValue());
        } else {
          clientToken.setExpireSeconds(expireSeconds);
        }
      }
    } else if (appClient.getExpireAt() == 0) {
      clientToken.setExpireSeconds(0L);
    } else {
      if (AliveTimeEnum.LONGEVITY.equals(appClient.getTokenAlive())) {
        clientToken.setExpireSeconds(-1L);
      } else {
        clientToken.setExpireSeconds(Constants.CLIENT_TOKEN_DURATION_SECONDS);
      }
    }

    // 将token持久化到数据库中，以备重启服务器后原token继续可用
    if (!StringUtils.equals(token, appClient.getAccessToken())) {
      appClientDao.updateTokenByAppKey(clientId, token);
    }

    Map<String, AccessToken> tokenClientMap = cacheFactory
        .getCacheMap(Constants.CACHE_KEY_TOKEN_CLIENT, AccessToken.class);
    tokenClientMap.put(token, clientToken);

    return clientToken;
  }

  public String verifyTokenAndGetAppKey(String tokenStr) {
    if (StringUtils.isBlank(tokenStr)) {
      return null;
    }
    long currentTimestamp = getCurrentTimestamp();
    Map<String, AccessToken> tokenClientMap = cacheFactory
        .getCacheMap(Constants.CACHE_KEY_TOKEN_CLIENT, AccessToken.class);
    AccessToken clientToken = tokenClientMap.get(tokenStr);
    if (null == clientToken) {
      if (tokenStr.length() == TokenUtils.getTokenStringLength()) {
        AppClientEntity appClient = appClientDao.getByAccessToken(tokenStr);
        if (null == appClient) {
          return null;
        }
        if ((AliveTimeEnum.LONGEVITY == appClient.getTokenAlive() & appClient.getExpireAt() < 0)
            || (appClient.getExpireAt() > currentTimestamp)) {
          clientToken = AccessToken.builder()
              .realName(appClient.getName())
              .appKey(appClient.getAppKey())
              .accessToken(tokenStr)
              .createTimestamp(appClient.getUpdateTime().getTime())
              .expireSeconds(appClient.getTokenAlive().getValue())
              .build();
          tokenClientMap.put(tokenStr, clientToken);
          return clientToken.getAppKey();
        }
      }
      return null;
    }
    long durationTimestamp = currentTimestamp - clientToken.getCreateTimestamp();
    long expireTimestamp = clientToken.getExpireSeconds();
    if (expireTimestamp <= 0) {
      if (0 == expireTimestamp) {
        // 一次性的应用客户端
        tokenClientMap.remove(tokenStr);
        log.warn("token [{}] only can use once, clientId: {}", tokenStr, clientToken.getAppKey());
      } else {
        // 长期性的应用客户端，使用长期的token情况
        return clientToken.getAppKey();
      }
    } else if (durationTimestamp > expireTimestamp) {
      log.warn("token [{}] expired, clientId: {}", tokenStr, clientToken.getAppKey());
      return null;
    }
    return clientToken.getAppKey();
  }

  public boolean verifyAuthGroup(String clientId, Long groupId) {
    return appClientDao.existsAuthGroups(clientId, groupId);
  }

  private long getCurrentTimestamp() {
    return System.currentTimeMillis() / 1000L;
  }
}
