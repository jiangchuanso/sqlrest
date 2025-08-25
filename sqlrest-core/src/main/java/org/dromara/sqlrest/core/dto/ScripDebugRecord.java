// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.dto;

import org.dromara.sqlrest.common.service.DisplayRecord;

public class ScripDebugRecord implements DisplayRecord {

  private String text;

  public ScripDebugRecord(String text) {
    this.text = text;
  }

  @Override
  public String getDisplayText() {
    return text;
  }
}
