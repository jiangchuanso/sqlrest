package com.gitee.sqlrest.script.parsing.ast.statement;

import com.gitee.sqlrest.script.asm.Label;
import com.gitee.sqlrest.script.asm.Opcodes;
import com.gitee.sqlrest.script.compile.MagicScriptCompiler;
import com.gitee.sqlrest.script.parsing.Span;
import com.gitee.sqlrest.script.parsing.ast.Expression;
import com.gitee.sqlrest.script.parsing.ast.Node;
import com.gitee.sqlrest.script.runtime.handle.OperatorHandle;

import java.util.List;

/**
 * assert expr : expr[,expr][,expr][,expr]
 */
public class Assert extends Node {

	private final Expression condition;

	private final List<Expression> expressions;

	public Assert(Span span, Expression condition, List<Expression> expressions) {
		super(span);
		this.condition = condition;
		this.expressions = expressions;
	}

	@Override
	public void visitMethod(MagicScriptCompiler compiler) {
		condition.visitMethod(compiler);
		expressions.forEach(it -> it.visitMethod(compiler));
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		Label end = new Label();
		compiler.visit(condition)
				.invoke(Opcodes.INVOKESTATIC, OperatorHandle.class, "isFalse", boolean.class, Object.class)
				.jump(Opcodes.IFEQ, end)
				.compile(new Exit(getSpan(), expressions))
				.label(end);
	}

}
