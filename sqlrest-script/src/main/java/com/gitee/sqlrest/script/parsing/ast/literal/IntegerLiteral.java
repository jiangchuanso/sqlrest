package com.gitee.sqlrest.script.parsing.ast.literal;

import com.gitee.sqlrest.script.MagicScriptError;
import com.gitee.sqlrest.script.compile.MagicScriptCompiler;
import com.gitee.sqlrest.script.parsing.Span;

/**
 * int常量
 */
public class IntegerLiteral extends NumberLiteral {

	public IntegerLiteral(Span literal) {
		super(literal);
	}

	public IntegerLiteral(Span span, Object value) {
		super(span, value);
	}

	@Override
	public void compile(MagicScriptCompiler context) {
		if(this.value == null){
			try {
				this.value = Integer.parseInt(getText().replace("_",""));
			} catch (NumberFormatException e) {
				MagicScriptError.error("定义int变量值不合法", getSpan(), e);
			}
		}
		context.visitInt((Integer) value)
				.invoke(INVOKESTATIC, Integer.class, "valueOf", Integer.class, int.class);
	}
}
