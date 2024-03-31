package com.gitee.sqlrest.script.parsing.ast.linq;

import com.gitee.sqlrest.script.compile.MagicScriptCompiler;
import com.gitee.sqlrest.script.parsing.Span;
import com.gitee.sqlrest.script.parsing.ast.Literal;

public class WholeLiteral extends Literal {

	public WholeLiteral(Span span) {
		super(span);
	}

	public WholeLiteral(Span span, Object value) {
		super(span, value);
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		compiler.load2();
	}
}
