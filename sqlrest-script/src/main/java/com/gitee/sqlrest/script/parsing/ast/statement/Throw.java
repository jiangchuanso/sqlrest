package com.gitee.sqlrest.script.parsing.ast.statement;

import com.gitee.sqlrest.script.compile.MagicScriptCompiler;
import com.gitee.sqlrest.script.exception.MagicScriptRuntimeException;
import com.gitee.sqlrest.script.parsing.Span;
import com.gitee.sqlrest.script.parsing.ast.Expression;
import com.gitee.sqlrest.script.parsing.ast.Node;

public class Throw extends Node {

	private final Expression expression;

	public Throw(Span span, Expression expression) {
		super(span);
		this.expression = expression;
	}

	@Override
	public void visitMethod(MagicScriptCompiler compiler) {
		expression.visitMethod(compiler);
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		// throw MagicScriptRuntimeException.create(expr);
		compiler.visit(expression)
				.invoke(INVOKESTATIC, MagicScriptRuntimeException.class, "create", MagicScriptRuntimeException.class, Object.class)
				.insn(ATHROW);
	}
}
