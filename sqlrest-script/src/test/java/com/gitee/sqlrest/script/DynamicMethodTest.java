package com.gitee.sqlrest.script;

import com.gitee.sqlrest.script.functions.DynamicMethod;

import java.util.List;
import java.util.stream.Collectors;

public class DynamicMethodTest implements DynamicMethod {

	@Override
	public Object execute(String methodName, List<Object> parameters) {
		return parameters.stream().map(Object::toString).collect(Collectors.joining(""));
	}
}
