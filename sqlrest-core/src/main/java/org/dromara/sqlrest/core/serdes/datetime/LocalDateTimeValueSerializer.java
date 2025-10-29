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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang3.StringUtils;
import org.dromara.sqlrest.core.util.JacksonUtils;

public class LocalDateTimeValueSerializer extends StdSerializer<LocalDateTime> {

  private static final String DEFAULT_PATTERN = DatePattern.NORM_DATETIME_PATTERN;

  private String pattern;

  public LocalDateTimeValueSerializer(String pattern) {
    super(LocalDateTime.class);
    this.pattern = StringUtils.defaultIfBlank(pattern, DEFAULT_PATTERN);
  }

  @Override
  public void serialize(LocalDateTime value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {
    if (value != null) {
      // 对于LocalDateTime，使用DateTimeFormatter而不是SimpleDateFormat
      // 从配置文件读取时区设置，如果未配置则使用默认值Asia/Shanghai
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern)
          .withZone(ZoneId.of(JacksonUtils.getTimezone()));
      jsonGenerator.writeString(value.format(DateTimeFormatter.ofPattern(pattern)));
    }
  }
}
