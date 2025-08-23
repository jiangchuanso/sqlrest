// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.common.service;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

public interface FlowControlManger {

  boolean checkFlowControl(String resourceName, HttpServletResponse response) throws IOException;
}
