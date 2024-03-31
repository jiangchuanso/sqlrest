package com.gitee.sqlrest.script.runtime.function;

import com.gitee.sqlrest.script.runtime.Variables;

@FunctionalInterface
public interface MagicScriptLambdaFunction {

	Object apply(Variables variables, Object[] args);
}
