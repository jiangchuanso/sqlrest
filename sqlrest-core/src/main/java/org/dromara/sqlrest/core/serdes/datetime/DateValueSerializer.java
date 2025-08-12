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
import java.sql.Date;
import java.text.SimpleDateFormat;
import org.apache.commons.lang3.StringUtils;

public class DateValueSerializer extends StdSerializer<Date> {

  private static final String DEFAULT_PATTERN = DatePattern.NORM_DATE_PATTERN;

  private String pattern;

  public DateValueSerializer(String pattern) {
    super(Date.class);
    this.pattern = StringUtils.defaultIfBlank(pattern, DEFAULT_PATTERN);
  }

  @Override
  public void serialize(Date value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {
    if (value != null) {
      SimpleDateFormat sdf = new SimpleDateFormat(pattern);
      jsonGenerator.writeString(sdf.format(value));
    }
  }

}
