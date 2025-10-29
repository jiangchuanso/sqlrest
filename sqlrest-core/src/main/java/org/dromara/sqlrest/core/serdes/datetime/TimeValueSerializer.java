// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.serdes.datetime;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.dromara.sqlrest.core.util.JacksonUtils;

public class TimeValueSerializer extends StdSerializer<Time> {

  private static final String DEFAULT_PATTERN = DatePattern.NORM_TIME_PATTERN;

  private String pattern;

  public TimeValueSerializer(String pattern) {
    super(Time.class);
    this.pattern = StringUtils.defaultIfBlank(pattern, DEFAULT_PATTERN);
  }

  @Override
  public void serialize(Time value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {
    if (value != null) {
      SimpleDateFormat sdf = new SimpleDateFormat(pattern);
      // 从配置文件读取时区设置，如果未配置则使用默认值Asia/Shanghai
      sdf.setTimeZone(TimeZone.getTimeZone(JacksonUtils.getTimezone()));
      jsonGenerator.writeString(sdf.format(value));
    }
  }
}
