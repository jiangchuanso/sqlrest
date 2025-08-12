// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.serdes;

import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.dromara.sqlrest.common.enums.DataTypeFormatEnum;
import org.dromara.sqlrest.core.serdes.datetime.DateValueSerializer;
import org.dromara.sqlrest.core.serdes.datetime.LocalDateTimeValueSerializer;
import org.dromara.sqlrest.core.serdes.datetime.LocalDateValueSerializer;
import org.dromara.sqlrest.core.serdes.datetime.TimeValueSerializer;
import org.dromara.sqlrest.core.serdes.datetime.TimestampValueSerializer;
import org.dromara.sqlrest.core.serdes.number.NumberValueSerializer;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class DateTimeSerDesFactory {

  private static Map<DataTypeFormatEnum, Function<String, StdSerializer>> DATE_TIME_SER_MAP = new HashMap<>();

  static {
    DATE_TIME_SER_MAP.put(DataTypeFormatEnum.DATE, DateValueSerializer::new);
    DATE_TIME_SER_MAP.put(DataTypeFormatEnum.LOCAL_DATE, LocalDateValueSerializer::new);

    DATE_TIME_SER_MAP.put(DataTypeFormatEnum.TIME, TimeValueSerializer::new);

    DATE_TIME_SER_MAP.put(DataTypeFormatEnum.TIMESTAMP, TimestampValueSerializer::new);
    DATE_TIME_SER_MAP.put(DataTypeFormatEnum.LOCAL_DATE_TIME, LocalDateTimeValueSerializer::new);

    DATE_TIME_SER_MAP.put(DataTypeFormatEnum.BIG_DECIMAL, NumberValueSerializer::new);
  }

  public static Map<DataTypeFormatEnum, Function<String, StdSerializer>> getAllSerDesMap() {
    return ImmutableMap.copyOf(DATE_TIME_SER_MAP);
  }
}
