/////////////////////////////////////////////////////////////
// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.common.util;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

@Slf4j
public class I18nUtils {

  private static MessageSource messageSource;

  private I18nUtils() {}

  private static MessageSource getMessageSource() {
    if (messageSource == null) {
      messageSource = SpringUtil.getBean(MessageSource.class);
    }
    return messageSource;
  }

  /**
   * 获取国际化消息
   *
   * @param key 消息键
   * @param args 消息参数
   * @return 本地化消息字符串
   */
  public static String getMessage(String key, Object... args) {
    MessageSource messageSource = getMessageSource();
    Locale locale = LocaleContextHolder.getLocale();
    try {
      return messageSource.getMessage(key, args, locale);
    } catch (Exception e) {
      log.warn("No key found for {}, use key as default.", key);
      return key;
    }
  }

  /**
   * 获取国际化消息，带默认值
   *
   * @param key 消息键
   * @param defaultMessage 默认消息
   * @param args 消息参数
   * @return 本地化消息字符串
   */
  public static String getMessage(String key, String defaultMessage, Object... args) {
    MessageSource messageSource = getMessageSource();
    Locale locale = LocaleContextHolder.getLocale();
    try {
      return messageSource.getMessage(key, args, defaultMessage, locale);
    } catch (Exception e) {
      log.warn("No key found for {}, use key as default.", key);
      return defaultMessage;
    }
  }

  /**
   * 获取国际化消息，指定 Locale（用于 WebFlux 等 RequestContext 不可用的场景）
   *
   * @param key 消息键
   * @param locale 区域
   * @param args 消息参数
   * @return 本地化消息字符串
   */
  public static String getMessage(String key, Locale locale, Object... args) {
    MessageSource messageSource = getMessageSource();
    try {
      return messageSource.getMessage(key, args, locale);
    } catch (Exception e) {
      log.warn("No key found for {}, use key as default.", key);
      return key;
    }
  }
}
