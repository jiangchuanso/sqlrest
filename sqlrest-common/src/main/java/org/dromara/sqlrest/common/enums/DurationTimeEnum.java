// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.common.enums;

import lombok.Getter;

@Getter
public enum DurationTimeEnum {

  FOR_EVER(-1),
  ONLY_ONCE(0),
  TIME_VALUE(1),
  ;

  private long value;

  DurationTimeEnum(long value) {
    this.value = value;
  }

}
