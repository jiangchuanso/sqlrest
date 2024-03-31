package com.gitee.sqlrest.script.parsing.ast.binary;

import com.gitee.sqlrest.script.compile.MagicScriptCompiler;
import com.gitee.sqlrest.script.parsing.Span;
import com.gitee.sqlrest.script.parsing.ast.BinaryOperation;
import com.gitee.sqlrest.script.parsing.ast.Expression;

/**
 * ==、===操作
 */
public class EqualOperation extends BinaryOperation {

	protected final boolean accurate;

	public EqualOperation(Expression leftOperand, Span span, Expression rightOperand, boolean accurate) {
		super(leftOperand, span, rightOperand);
		this.accurate = accurate;
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		compiler.visit(getLeftOperand())
				.visit(getRightOperand())
				.lineNumber(getSpan())
				.operator(accurate ? "accurate_equals" : "equals");
	}
}
