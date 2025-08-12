// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;

public class XmlSqlTemplate {

  private static final Pattern pattern = Pattern.compile("\\([^()]*\\)|\\[[^\\[\\]]*\\]|\\{[^{}]*}");

  private Configuration configuration;
  private XNode root;

  public XmlSqlTemplate(String xmlSql) {
    configuration = new Configuration();
    configuration.setMapUnderscoreToCamelCase(true);
    this.root = parseXml(xmlSql);
  }

  private XNode parseXml(String sql) {
    String xml = "<script>" + sql + "</script>";
    XPathParser parser = new XPathParser(xml);
    List<XNode> xNodes = parser.evalNodes("script");
    if (xNodes == null || xNodes.size() <= 0) {
      throw new RuntimeException("Can not find sql statement from text: " + sql);
    }
    if (xNodes.size() > 1) {
      throw new RuntimeException("Only support one sql statement for parse");
    }

    return xNodes.get(0);
  }

  public Map<String, Boolean> getParameterNames() {
    Map<String, Boolean> inputParams = new LinkedHashMap<>();
    XmlScriptBuilder builder = new XmlScriptBuilder(configuration, root);
    builder.parseScriptNode(inputParams);

    Map<String, Boolean> names = new LinkedHashMap<>();
    for (Map.Entry<String, Boolean> entry : inputParams.entrySet()) {
      String name = entry.getKey();
      String subName = null;
      if (null == entry.getValue()) {
        continue;
      }
      Matcher matcher = pattern.matcher(name);
      while (matcher.find()) {
        name = name.replaceAll(Pattern.quote(matcher.group()), "");
        matcher = pattern.matcher(name);
      }

      int commaIdx = name.indexOf(",");
      if (commaIdx > 0) {
        name = name.substring(0, commaIdx);
      }

      int idx = name.indexOf(".");
      if (idx > 0) {
        subName = name;
        name = name.substring(0, idx);
        if (entry.getValue()) {
          names.put(name, false);
          if (null != subName) {
            names.put(subName, true);
          }
        } else {
          names.putIfAbsent(name, entry.getValue());
          if (null != subName) {
            names.putIfAbsent(subName, entry.getValue());
          }
        }
        continue;
      } else {
        if (entry.getValue()) {
          names.put(name, true);
          if (null != subName) {
            names.put(subName, true);
          }
        } else {
          names.putIfAbsent(name, entry.getValue());
          if (null != subName) {
            names.putIfAbsent(subName, entry.getValue());
          }
        }
      }
    }

    return names;
  }

  public SqlMeta process(Map<String, Object> params) {
    XmlScriptBuilder builder = new XmlScriptBuilder(configuration, root);
    Map<String, Boolean> inputParams = new HashMap<>();
    SqlSource sqlSource = builder.parseScriptNode(inputParams);
    BoundSql boundSql = sqlSource.getBoundSql(params);

    List<Object> paramValues = new ArrayList<>();
    for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
      String name = parameterMapping.getProperty();
      Object value = boundSql.getAdditionalParameter(name);
      if (null == value) {
        int idx = name.indexOf(".");
        if (idx > 0) {
          String objName = name.substring(0, idx);
          String subName = name.substring(idx + 1);
          value = params.get(objName);
          if (value instanceof Map) {
            value = ((Map) value).get(subName);
          }
        } else {
          value = params.get(name);
        }
      }
      paramValues.add(value);
    }

    return new SqlMeta(boundSql.getSql(), paramValues);
  }
}