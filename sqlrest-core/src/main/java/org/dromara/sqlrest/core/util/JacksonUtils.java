// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.collect.Lists;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.sqlrest.common.dto.OutParam;
import org.dromara.sqlrest.common.enums.DataTypeFormatEnum;
import org.dromara.sqlrest.common.enums.ParamTypeEnum;
import org.dromara.sqlrest.common.util.UuidUtils;
import org.dromara.sqlrest.core.serdes.DateTimeSerDesFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public final class JacksonUtils {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static Environment environment;
  
  // 日志控制标志，避免频繁打印相同日志
  private static boolean hasLoggedEnvironmentWarning = false;
  private static boolean hasLoggedConfigRead = false;

  @Autowired
  public void setEnvironment(Environment env) {
    environment = env;
    log.info("Spring Environment已注入，重新初始化ObjectMapper时区设置");
    // 重置日志标志，允许记录新的配置读取日志
    hasLoggedConfigRead = false;
    initializeObjectMapper();
  }

  /**
   * 初始化ObjectMapper的时区设置
   */
  public static void initializeObjectMapper() {
    String timezone = getTimezone();
    objectMapper.setTimeZone(TimeZone.getTimeZone(timezone));
  }

  static {
    objectMapper.disable(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS);
    // 静态初始化时使用默认时区，等Spring容器启动后再重新设置
    objectMapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    log.info("JacksonUtils静态初始化完成，使用默认时区: Asia/Shanghai");
  }

  /**
   * 获取配置的时区，如果未配置则返回默认时区
   */
  public static String getTimezone() {
    String timezone;
    if (environment != null) {
      timezone = environment.getProperty("JSON_TIMEZONE", "Asia/Shanghai");
      // 只在第一次成功读取配置时记录日志，避免频繁打印
      if (!timezone.equals("Asia/Shanghai") || !hasLoggedConfigRead) {
        log.info("从配置文件读取时区设置: JSON_TIMEZONE={}", timezone);
        hasLoggedConfigRead = true;
      }
    } else {
      timezone = "Asia/Shanghai";
      // 只在第一次警告时记录日志，避免频繁打印
      if (!hasLoggedEnvironmentWarning) {
        log.warn("Spring Environment未初始化，使用默认时区: {}。请检查启动脚本是否正确设置了JSON_TIMEZONE环境变量", timezone);
        hasLoggedEnvironmentWarning = true;
      }
    }
    return timezone;
  }

  public static String toJsonStr(Object obj, Map<DataTypeFormatEnum, String> formatMap) {
    ObjectMapper mapper = objectMapper.copy();
    mapper.registerModule(createSerializeModule(formatMap));
    try {
      return mapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private static Module createSerializeModule(Map<DataTypeFormatEnum, String> formatMap) {
    Map<DataTypeFormatEnum, String> finalFormatMap = (null == formatMap) ? Collections.emptyMap() : formatMap;
    SimpleModule module = new SimpleModule();
    DateTimeSerDesFactory.getAllSerDesMap()
        .forEach(
            (clazz, creator) -> {
              StdSerializer serializer = creator.apply(finalFormatMap.get(clazz));
              module.addSerializer(serializer.handledType(), serializer);
            }
        );
    return module;
  }

  public static List<OutParam> parseFiledTypesAndFillNullAsString(Object obj) {
    List<OutParam> types = parseFieldTypes(obj);
    for (OutParam param : types) {
      if (null == param.getType()) {
        param.setType(ParamTypeEnum.STRING);
      }
      if (CollectionUtils.isNotEmpty(param.getChildren())) {
        for (OutParam subParam : param.getChildren()) {
          if (null == subParam.getType()) {
            subParam.setType(ParamTypeEnum.STRING);
          }
        }
      }
    }
    return types;
  }

  public static List<OutParam> parseFieldTypes(Object obj) {
    List<OutParam> results = new LinkedList<>();
    if (null == obj) {
      return results;
    }

    if (obj instanceof Map) {
      parseFieldTypes(null, (Map) obj, results);
    } else if (obj instanceof Collection) {
      Collection collection = (Collection) obj;
      if (CollectionUtils.isEmpty(collection)) {
        return results;
      }
      for (Object item : collection) {
        if (item instanceof Map) {
          parseFieldTypes(null, (Map) item, results);
        } else if (item instanceof Collection) {
          Collection subCollection = (Collection) item;
          if (CollectionUtils.isEmpty(subCollection)) {
            return results;
          }
          for (Object subItem : subCollection) {
            if (subItem instanceof Map) {
              parseFieldTypes(null, (Map) subItem, results);
            } else if (subItem instanceof Collection) {
              Collection thSubCollection = (Collection) subItem;
              if (CollectionUtils.isEmpty(thSubCollection)) {
                return results;
              }
              for (Object thSubItem : thSubCollection) {
                if (thSubItem instanceof Map) {
                  parseFieldTypes(null, (Map) thSubItem, results);
                }
              }
            }
          }
        }
      }
    }
    return results;
  }

  private static void parseFieldTypes(OutParam parent, Map<String, Object> map, List<OutParam> results) {
    for (String name : map.keySet()) {
      Object value = map.get(name);
      boolean isArray = (value instanceof Collection);
      ParamTypeEnum typeEnum = parseValueType(value);
      String id = UuidUtils.generateUuid();
      OutParam outParam = new OutParam(id, name, typeEnum, isArray, null, Lists.newArrayList());
      if (isArray) {
        outParam.setType(parseValueType(((Collection) value).stream().findFirst().orElse(null)));
      }
      if (null == parent) {
        if (results.stream().noneMatch(one -> name.equals(one.getName()))) {
          results.add(outParam);
        }
      } else {
        if (parent.getChildren().stream().noneMatch(one -> name.equals(one.getName()))) {
          parent.getChildren().add(outParam);
        }
      }

      if (isArray) {
        Collection collection = (Collection) value;
        if (collection.size() > 0) {
          Object item = collection.stream().findFirst().get();
          ParamTypeEnum subTypeEnum = parseValueType(item);
          if (null != subTypeEnum && subTypeEnum.isObject()) {
            parseFieldTypes(outParam, (Map) item, results);
          }
        }
      } else if (value instanceof Map) {
        parseFieldTypes(outParam, (Map) value, results);
      }
    }
  }

  private static ParamTypeEnum parseValueType(Object value) {
    if (value instanceof Boolean || value instanceof Byte) {
      return ParamTypeEnum.BOOLEAN;
    } else if (value instanceof Integer || value instanceof Long || value instanceof BigInteger) {
      return ParamTypeEnum.LONG;
    } else if (value instanceof Number) {
      return ParamTypeEnum.DOUBLE;
    } else if (value instanceof Time || value instanceof Timestamp || value instanceof LocalDateTime) {
      return ParamTypeEnum.TIME;
    } else if (value instanceof Date || value instanceof LocalDate) {
      return ParamTypeEnum.DATE;
    } else if (value instanceof String) {
      return ParamTypeEnum.STRING;
    } else if (value instanceof Map) {
      return ParamTypeEnum.OBJECT;
    } else {
      // List的情况返回null
      return null;
    }
  }
}
