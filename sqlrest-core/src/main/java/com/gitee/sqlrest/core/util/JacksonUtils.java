package com.gitee.sqlrest.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.gitee.sqlrest.common.enums.DataTypeFormatEnum;
import com.gitee.sqlrest.common.enums.ParamTypeEnum;
import com.gitee.sqlrest.core.serdes.DateTimeSerDesFactory;
import java.math.BigInteger;
import java.sql.Time;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public final class JacksonUtils {

  public static String toJsonStr(Object obj) {
    return toJsonStr(obj, Collections.emptyMap());
  }

  public static String toJsonStr(Object obj, Map<DataTypeFormatEnum, String> formatMap) {
    // https://www.jianshu.com/p/1368547350c6
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(createSerializeModule(formatMap));
    try {
      return objectMapper.writeValueAsString(obj);
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

  public static Map<String, ParamTypeEnum> parseFieldTypes(Object obj) {
    Map<String, ParamTypeEnum> results = new LinkedHashMap<>();
    if (null == obj) {
      return results;
    }

    if (obj instanceof Map) {
      parseFieldTypes("", (Map) obj, results);
    } else if (obj instanceof Collection) {
      Collection collection = (Collection) obj;
      Object item = collection.stream().findFirst().get();
      if (item instanceof Map) {
        parseFieldTypes("", (Map) item, results);
      } else if (item instanceof Collection) {
        Collection subCollection = (Collection) item;
        Object subItem = subCollection.stream().findFirst().get();
        if (subItem instanceof Map) {
          parseFieldTypes("", (Map) subItem, results);
        } else if (subItem instanceof Collection) {
          Collection thSubCollection = (Collection) subItem;
          Object thSubItem = thSubCollection.stream().findFirst().get();
          if (thSubItem instanceof Map) {
            parseFieldTypes("", (Map) thSubItem, results);
          }
        }
      }
    }
    return results;
  }

  private static void parseFieldTypes(String prefix, Map<String, Object> map, Map<String, ParamTypeEnum> results) {
    for (String name : map.keySet()) {
      Object value = map.get(name);
      ParamTypeEnum typeEnum = parseValueType(value);
      if (null != typeEnum) {
        // TODO: 暂时先忽略不支持的对象的情况
        if (StringUtils.isBlank(prefix)) {
          results.put(name, typeEnum);
        } else {
          results.put(prefix + "." + name, typeEnum);
        }
      }
      String subPrefix = StringUtils.isBlank(prefix) ? name : prefix + "." + name;
      if (value instanceof Map) {
        parseFieldTypes(subPrefix, (Map) value, results);
      } else if (value instanceof Collection) {
        Collection collection = (Collection) value;
        Object item = collection.stream().findFirst().get();
        parseFieldTypes(subPrefix, (Map) item, results);
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
    } else if (value instanceof Time) {
      return ParamTypeEnum.TIME;
    } else if (value instanceof Date) {
      return ParamTypeEnum.DATE;
    } else if (value instanceof String) {
      return ParamTypeEnum.STRING;
    } else {
      return null;
    }
  }
}
