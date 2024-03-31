package com.gitee.sqlrest.script.functions;

import java.util.List;

public interface DynamicMethod {

	Object execute(String methodName, List<Object> parameters);

}