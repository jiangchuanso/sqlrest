package com.gitee.sqlrest.script.functions;

import java.util.Collections;
import java.util.List;

public interface ExtensionMethod {

	default List<Class<?>> supports() {
		return Collections.singletonList(support());
	}

	Class<?> support();
}
