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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang3.StringUtils;

public class LocalDateValueSerializer extends StdSerializer<LocalDate> {

  private static final String DEFAULT_PATTERN = DatePattern.NORM_DATE_PATTERN;

  private String pattern;

  public LocalDateValueSerializer(String pattern) {
    super(LocalDate.class);
    this.pattern = StringUtils.defaultIfBlank(pattern, DEFAULT_PATTERN);
  }

  @Override
  public void serialize(LocalDate value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {
    if (value != null) {
      // 对于LocalDate，使用DateTimeFormatter而不是SimpleDateFormat
      // 从配置文件读取时区设置，如果未配置则使用默认值Asia/Shanghai
      jsonGenerator.writeString(value.format(DateTimeFormatter.ofPattern(pattern)));
    }
  }
}
