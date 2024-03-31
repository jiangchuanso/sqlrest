package com.gitee.sqlrest.script.parsing.ast.binary;

import com.gitee.sqlrest.script.compile.MagicScriptCompiler;
import com.gitee.sqlrest.script.parsing.Span;
import com.gitee.sqlrest.script.parsing.ast.BinaryOperation;
import com.gitee.sqlrest.script.parsing.ast.Expression;

/**
 * ^
 */
public class XorOperation extends BinaryOperation {

	public XorOperation(Expression leftOperand, Span span, Expression rightOperand) {
		super(leftOperand, span, rightOperand);
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		compiler.visit(getLeftOperand())
				.visit(getRightOperand())
				.lineNumber(getSpan())
				.bit("xor");
	}
}
