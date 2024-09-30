package com.gitee.sqlrest.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.gitee.sqlrest.common.enums.DataTypeFormatEnum;
import com.gitee.sqlrest.common.enums.NamingStrategyEnum;
import com.gitee.sqlrest.core.serdes.DateTimeSerDesFactory;
import java.util.Collections;
import java.util.Map;

public final class JacksonUtils {

  public static String toJsonStr(Object obj) {
    return toJsonStr(obj, Collections.emptyMap(), null);
  }

  public static String toJsonStr(Object obj, Map<DataTypeFormatEnum, String> formatMap, NamingStrategyEnum namingStrategy) {
    // https://www.jianshu.com/p/1368547350c6
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(createSerializeModule(formatMap));
    if (null != namingStrategy) {
      PropertyNamingStrategy strategy = createPropertyNamingStrategy(namingStrategy);
      if (null != strategy) {
        objectMapper.setPropertyNamingStrategy(strategy);
      }
    }

    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private static PropertyNamingStrategy createPropertyNamingStrategy(NamingStrategyEnum namingStrategy) {
    if (namingStrategy == NamingStrategyEnum.LOWER_CAMEL_CASE) {
      return PropertyNamingStrategies.LOWER_CAMEL_CASE;
    } else if (namingStrategy == NamingStrategyEnum.UPPER_CAMEL_CASE) {
      return PropertyNamingStrategies.UPPER_CAMEL_CASE;
    } else if (namingStrategy == NamingStrategyEnum.SNAKE_CASE) {
      return PropertyNamingStrategies.SNAKE_CASE;
    } else if (namingStrategy == NamingStrategyEnum.LOWER_CASE) {
      return PropertyNamingStrategies.LOWER_CASE;
    } else {
      return null;
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

}
