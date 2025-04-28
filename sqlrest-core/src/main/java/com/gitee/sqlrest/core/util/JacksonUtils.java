package com.gitee.sqlrest.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.gitee.sqlrest.common.dto.OutParam;
import com.gitee.sqlrest.common.enums.DataTypeFormatEnum;
import com.gitee.sqlrest.common.enums.ParamTypeEnum;
import com.gitee.sqlrest.common.util.UuidUtils;
import com.gitee.sqlrest.core.serdes.DateTimeSerDesFactory;
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
import org.apache.commons.collections4.CollectionUtils;

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
        Object item = collection.stream().findFirst().get();
        ParamTypeEnum subTypeEnum = parseValueType(item);
        if (null != subTypeEnum && subTypeEnum.isObject()) {
          parseFieldTypes(outParam, (Map) item, results);
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
