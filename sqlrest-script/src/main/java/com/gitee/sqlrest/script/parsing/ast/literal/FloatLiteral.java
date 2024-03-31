package com.gitee.sqlrest.script.parsing.ast.literal;

import com.gitee.sqlrest.script.MagicScriptError;
import com.gitee.sqlrest.script.compile.MagicScriptCompiler;
import com.gitee.sqlrest.script.parsing.Span;

/**
 * float常量
 */
public class FloatLiteral extends NumberLiteral {

	public FloatLiteral(Span literal) {
		super(literal);
	}

	@Override
	public void compile(MagicScriptCompiler context) {
		if(this.value == null) {
			try {
				setValue(Float.parseFloat(getText().replace("_", "")));
			} catch (NumberFormatException e) {
				MagicScriptError.error("定义float变量值不合法", getSpan(), e);
			}
		}
		context.ldc(value).invoke(INVOKESTATIC, Float.class, "valueOf", Float.class, float.class);
	}
}
