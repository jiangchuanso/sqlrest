// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.util;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

@Slf4j
public final class SpelUtils {

  private final static ExpressionParser expressionParser = new SpelExpressionParser();

  public static String getExpressionValue(String expr, Map<String, Object> paramValues) {
    EvaluationContext context = new StandardEvaluationContext();
    for (Map.Entry<String, Object> entry : paramValues.entrySet()) {
      context.setVariable(entry.getKey(), entry.getValue());
    }
    try {
      Object value = expressionParser.parseExpression(expr).getValue(context);
      if (null != value) {
        return value.toString();
      }
    } catch (Exception e) {
      log.warn("Parse SpEL value from parameters failed:{}", e.getMessage());
    }
    return expr;
  }
}
