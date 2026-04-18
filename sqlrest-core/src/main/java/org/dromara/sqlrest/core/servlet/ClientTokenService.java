// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.servlet;

import org.dromara.sqlrest.cache.CacheFactory;
import org.dromara.sqlrest.common.consts.Constants;
import org.dromara.sqlrest.common.dto.AccessToken;
import org.dromara.sqlrest.common.enums.AliveTimeEnum;
import org.dromara.sqlrest.common.enums.DurationTimeEnum;
import org.dromara.sqlrest.common.exception.CommonException;
import org.dromara.sqlrest.common.exception.ResponseErrorCode;
import org.dromara.sqlrest.common.util.TokenUtils;
import org.dromara.sqlrest.persistence.dao.AppClientDao;
import org.dromara.sqlrest.persistence.entity.AppClientEntity;
import org.dromara.sqlrest.persistence.util.JsonUtils;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Objects;
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
    Map<String, AccessToken> tokenClientMap = cacheFactory
        .getCacheMap(Constants.CACHE_KEY_TOKEN_CLIENT, AccessToken.class);
    try {
      for (AppClientEntity appClient : appClientDao.listAll()) {
        appClient.setAppSecret("******");
        if (StringUtils.isNotBlank(appClient.getAccessToken())) {
          if (isTokenExpired(appClient, currentTimestamp)) {
            log.info("Remove expired client token from persistence, appKey:{}", appClient.getAppKey());
            appClientDao.clearTokenByAppKey(appClient.getAppKey());
            continue;
          }

          if (isOneTimeToken(appClient)) {
            log.debug("Skip restoring one-time token for appKey:{}", appClient.getAppKey());
            continue;
          }

          AccessToken clientToken = buildAccessTokenFromPersistence(appClient, currentTimestamp);
          log.info("Load client app token from persistence :{}", JsonUtils.toJsonString(appClient));
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
      throw new CommonException(ResponseErrorCode.ERROR_CLIENT_FORBIDDEN, "client.id.invalid");
    }
    if (!StringUtils.equals(appClient.getAppSecret(), clientSecret)) {
      throw new CommonException(ResponseErrorCode.ERROR_CLIENT_FORBIDDEN, "client.secret.invalid");
    }
    if (DurationTimeEnum.TIME_VALUE == appClient.getExpireDuration()) {
      Boolean isExpired = getCurrentTimestamp() > appClient.getExpireAt();
      if (isExpired) {
        throw new CommonException(ResponseErrorCode.ERROR_CLIENT_FORBIDDEN, "client.id.expired");
      }
    } else if (DurationTimeEnum.ONLY_ONCE == appClient.getExpireDuration()) {
      if (!appClient.getCreateTime().equals(appClient.getUpdateTime())) {
        throw new CommonException(ResponseErrorCode.ERROR_CLIENT_FORBIDDEN, "client.id.expired");
      }
    }

    String token = TokenUtils.generateValue();
    Long createTimestamp = getCurrentTimestamp();
    if (AliveTimeEnum.LONGEVITY.equals(appClient.getTokenAlive())
        && StringUtils.isNotBlank(appClient.getAccessToken())) {
      token = appClient.getAccessToken();
      createTimestamp = toEpochSeconds(appClient.getUpdateTime());
    }

    AccessToken clientToken = AccessToken.builder()
        .realName(appClient.getName())
        .appKey(clientId)
        .accessToken(token)
        .createTimestamp(createTimestamp)
        .build();
    clientToken.setExpireSeconds(resolveExpireSeconds(appClient, createTimestamp, getCurrentTimestamp()));
    if (clientToken.getExpireSeconds() == 0L && DurationTimeEnum.ONLY_ONCE != appClient.getExpireDuration()) {
      // 防止自定义配置导致0秒，至少兜底一段时间
      clientToken.setExpireSeconds(Constants.CLIENT_TOKEN_DURATION_SECONDS);
    }

    // 将token持久化到数据库中，以备重启服务器后原token继续可用
    if (!StringUtils.equals(token, appClient.getAccessToken())) {
      appClientDao.updateTokenByAppKey(clientId, token);
    }

    Map<String, AccessToken> tokenClientMap = cacheFactory
        .getCacheMap(Constants.CACHE_KEY_TOKEN_CLIENT, AccessToken.class);
    if (clientToken.getExpireSeconds() == 0L) {
      tokenClientMap.remove(token);
    } else {
      tokenClientMap.put(token, clientToken);
    }

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
        if (isTokenExpired(appClient, currentTimestamp)) {
          appClientDao.clearTokenByAppKey(appClient.getAppKey());
          return null;
        }

        clientToken = buildAccessTokenFromPersistence(appClient, currentTimestamp);
        if (!isOneTimeToken(appClient)) {
          tokenClientMap.put(tokenStr, clientToken);
        }
        return clientToken.getAppKey();
      }
      return null;
    }
    long durationTimestamp = currentTimestamp - clientToken.getCreateTimestamp();
    long expireTimestamp = clientToken.getExpireSeconds();
    if (expireTimestamp <= 0) {
      if (0 == expireTimestamp) {
        // 一次性的应用客户端
        tokenClientMap.remove(tokenStr);
        appClientDao.clearTokenByAppKey(clientToken.getAppKey());
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

  private boolean isTokenExpired(AppClientEntity appClient, long currentTimestamp) {
    Long expireAt = appClient.getExpireAt();
    if (Objects.nonNull(expireAt) && expireAt > 0) {
      return expireAt <= currentTimestamp;
    }
    if (DurationTimeEnum.TIME_VALUE == appClient.getExpireDuration()) {
      return true;
    }
    return false;
  }

  private boolean isOneTimeToken(AppClientEntity appClient) {
    return DurationTimeEnum.ONLY_ONCE == appClient.getExpireDuration();
  }

  private AccessToken buildAccessTokenFromPersistence(AppClientEntity appClient, long currentTimestamp) {
    long createTimestamp = toEpochSeconds(appClient.getUpdateTime());
    long expireSeconds = resolveExpireSeconds(appClient, createTimestamp, currentTimestamp);
    return AccessToken.builder()
        .realName(appClient.getName())
        .appKey(appClient.getAppKey())
        .accessToken(appClient.getAccessToken())
        .createTimestamp(createTimestamp)
        .expireSeconds(expireSeconds)
        .build();
  }

  private long resolveExpireSeconds(AppClientEntity appClient, long createTimestamp, long currentTimestamp) {
    Long expireAt = appClient.getExpireAt();
    if (Objects.nonNull(expireAt)) {
      if (expireAt > 0) {
        long secondsLeft = expireAt - currentTimestamp;
        if (secondsLeft <= 0) {
          return 0L;
        }
        if (AliveTimeEnum.LONGEVITY.equals(appClient.getTokenAlive())) {
          long basedOnCreate = expireAt - createTimestamp;
          return Math.max(basedOnCreate, 0L);
        }
        return Math.min(secondsLeft, appClient.getTokenAlive().getValue());
      }
      if (expireAt == 0) {
        return 0L;
      }
    }

    if (AliveTimeEnum.LONGEVITY.equals(appClient.getTokenAlive())) {
      return -1L;
    }
    return appClient.getTokenAlive().getValue();
  }

  private long toEpochSeconds(Timestamp timestamp) {
    if (timestamp == null) {
      return getCurrentTimestamp();
    }
    return timestamp.getTime() / 1000;
  }
}
