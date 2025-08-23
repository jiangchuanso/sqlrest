// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.exec;

import org.dromara.sqlrest.persistence.entity.ApiAssignmentEntity;

public class ApiAssignmentCache {

  private static final ThreadLocal<ApiAssignmentEntity> THREAD_LOCAL = new ThreadLocal<>();

  public static void set(ApiAssignmentEntity entity) {
    THREAD_LOCAL.set(entity);
  }

  public static ApiAssignmentEntity get() {
    return THREAD_LOCAL.get();
  }

  public static void remove() {
    THREAD_LOCAL.remove();
  }
}
