package com.gitee.sqlrest.script.convert;

import com.gitee.sqlrest.script.runtime.Variables;
import com.gitee.sqlrest.script.parsing.ast.literal.BooleanLiteral;

/**
 * 任意值到boolean类型的隐式转换
 */
public class BooleanImplicitConvert implements ClassImplicitConvert {
	@Override
	public boolean support(Class<?> from, Class<?> to) {
		return to == Boolean.class || to == boolean.class;
	}

	@Override
	public Object convert(Variables variables, Object source, Class<?> target) {
		return BooleanLiteral.isTrue(source);
	}
}
