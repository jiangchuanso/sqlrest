package com.gitee.sqlrest.script.parsing.ast.literal;

import com.gitee.sqlrest.script.compile.MagicScriptCompiler;
import com.gitee.sqlrest.script.parsing.Span;
import com.gitee.sqlrest.script.parsing.ast.Literal;

/**
 * null 常量
 */
public class NullLiteral extends Literal {
	public NullLiteral(Span span) {
		super(span);
	}

	@Override
	public void compile(MagicScriptCompiler context) {
		context.insn(ACONST_NULL);
	}
}