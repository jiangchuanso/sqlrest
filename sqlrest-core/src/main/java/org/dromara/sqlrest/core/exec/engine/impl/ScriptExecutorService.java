// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/sqlrest
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2024/3/31
// Location: beijing , china
/////////////////////////////////////////////////////////////
package org.dromara.sqlrest.core.exec.engine.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.zaxxer.hikari.HikariDataSource;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.codehaus.groovy.control.CompilationFailedException;
import org.dromara.sqlrest.common.enums.NamingStrategyEnum;
import org.dromara.sqlrest.common.enums.ProductTypeEnum;
import org.dromara.sqlrest.common.service.VarModuleInterface;
import org.dromara.sqlrest.core.dto.ScriptEditorCompletion;
import org.dromara.sqlrest.core.exec.annotation.Module;
import org.dromara.sqlrest.core.exec.engine.AbstractExecutorEngine;
import org.dromara.sqlrest.core.exec.module.CacheVarModule;
import org.dromara.sqlrest.core.exec.module.DbVarModule;
import org.dromara.sqlrest.core.exec.module.DsVarModule;
import org.dromara.sqlrest.core.exec.module.EnvVarModule;
import org.dromara.sqlrest.core.exec.module.HttpModule;
import org.dromara.sqlrest.core.exec.module.LogVarModule;
import org.dromara.sqlrest.core.exec.module.ReqVarModule;
import org.dromara.sqlrest.core.exec.module.TxVarModule;
import org.dromara.sqlrest.persistence.entity.ApiContextEntity;

public class ScriptExecutorService extends AbstractExecutorEngine {

  public static List<ScriptEditorCompletion> syntax = new ArrayList<>();
  public static List<Class> modules = Arrays.asList(
      EnvVarModule.class,
      HttpModule.class,
      LogVarModule.class,
      DbVarModule.class,
      TxVarModule.class,
      DsVarModule.class,
      ReqVarModule.class,
      CacheVarModule.class);

  static {
    syntax.add(
        ScriptEditorCompletion.builder()
            .meta("foreach")
            .caption("foreach")
            .value("for(item in collection){\n\t\n}")
            .build());
    syntax.add(
        ScriptEditorCompletion.builder()
            .meta("for")
            .caption("fori")
            .value("for(i=0;i< ;i++){\n\t\n}")
            .build());
    syntax.add(
        ScriptEditorCompletion.builder()
            .meta("for")
            .caption("for")
            .value("for( ){\n\t\n}")
            .build());
    syntax.add(
        ScriptEditorCompletion.builder()
            .meta("if")
            .caption("if")
            .value("if( ){\n\n}")
            .build());
    syntax.add(
        ScriptEditorCompletion.builder()
            .meta("if")
            .caption("ifelse")
            .value("if( ){\n\t\n}else{\n\t\n}")
            .build());
    syntax.add(
        ScriptEditorCompletion.builder()
            .meta("import")
            .caption("import")
            .value("import ")
            .build());
    syntax.add(
        ScriptEditorCompletion.builder()
            .meta("continue")
            .caption("continue")
            .value("continue;")
            .build());
    syntax.add(
        ScriptEditorCompletion.builder()
            .meta("break")
            .caption("break")
            .value("break;")
            .build());
  }

  public static String getModuleVarName(Class clazz) {
    if (clazz.isAnnotationPresent(Module.class)) {
      Module annotation = (Module) clazz.getAnnotation(Module.class);
      return annotation.value();
    }
    return "unknown";
  }

  ////////////////////////////////////////////////////////////////////////////////////////

  public ScriptExecutorService(HikariDataSource dataSource, ProductTypeEnum productType) {
    super(dataSource, productType);
  }

  private String getModuleVarName(VarModuleInterface varModule) {
    return varModule.getVarModuleName();
  }

  private List<VarModuleInterface> getAllVarModules(Map<String, Object> params, NamingStrategyEnum strategy) {
    List<VarModuleInterface> moduleList = new ArrayList<>();
    moduleList.addAll(SpringUtil.getBeansOfType(VarModuleInterface.class).values());
    moduleList.add(new ReqVarModule(params));
    moduleList.add(new DsVarModule(params, strategy, printSqlLog));
    moduleList.add(new DbVarModule(dataSource, productType, params, strategy, printSqlLog));
    return moduleList;
  }

  @Override
  public List<Object> execute(List<ApiContextEntity> scripts, Map<String, Object> params, NamingStrategyEnum strategy) {
    List<VarModuleInterface> varModuleList = getAllVarModules(params, strategy);

    List<Object> results = new ArrayList<>();
    for (ApiContextEntity entity : scripts) {
      Binding binding = new Binding();
      params.forEach((k, v) -> binding.setProperty(k, v));
      varModuleList.forEach(m -> binding.setProperty(getModuleVarName(m), m));

      GroovyShell groovyShell = new GroovyShell(binding);
      try {
        results.add(groovyShell.evaluate(entity.getSqlText()));
      } catch (CompilationFailedException e) {
        throw new RuntimeException(e);
      }
    }
    return results;
  }

}
