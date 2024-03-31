package com.gitee.sqlrest.core.exec.engine.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.gitee.sqlrest.common.enums.ProductTypeEnum;
import com.gitee.sqlrest.core.exec.engine.AbstractExecutorEngine;
import com.gitee.sqlrest.core.exec.module.DataBaseModule;
import com.gitee.sqlrest.core.exec.module.EnvironmentModule;
import com.gitee.sqlrest.persistence.entity.ApiContextEntity;
import com.gitee.sqlrest.script.MagicScript;
import com.gitee.sqlrest.script.MagicScriptContext;
import com.gitee.sqlrest.script.MagicScriptEngineFactory;
import com.zaxxer.hikari.HikariDataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

public class ScriptExecutorService extends AbstractExecutorEngine {

  public ScriptExecutorService(HikariDataSource dataSource, ProductTypeEnum productType) {
    super(dataSource, productType);
  }

  public Object execute(List<ApiContextEntity> scripts, Map<String, Object> params) {
    ScriptEngineFactory scriptEngineFactory = new MagicScriptEngineFactory();
    ScriptEngine scriptEngine = scriptEngineFactory.getScriptEngine();
    List<Object> results = new ArrayList<>();
    for (ApiContextEntity entity : scripts) {
      MagicScript script = MagicScript.create(entity.getSqlText(), scriptEngine);
      MagicScriptContext context = new MagicScriptContext(params);
      context.set("db", new DataBaseModule(dataSource, productType));
      context.set("env", SpringUtil.getBean(EnvironmentModule.class));
      results.add(script.execute(context));
    }
    return results;
  }

}
