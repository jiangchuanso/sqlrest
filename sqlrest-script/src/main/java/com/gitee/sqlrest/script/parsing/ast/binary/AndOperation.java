package com.gitee.sqlrest.script.parsing.ast.binary;

import com.gitee.sqlrest.script.asm.Label;
import com.gitee.sqlrest.script.compile.MagicScriptCompiler;
import com.gitee.sqlrest.script.parsing.Span;
import com.gitee.sqlrest.script.runtime.handle.OperatorHandle;
import com.gitee.sqlrest.script.parsing.ast.BinaryOperation;
import com.gitee.sqlrest.script.parsing.ast.Expression;

/**
 * && 操作
 */
public class AndOperation extends BinaryOperation {

	public AndOperation(Expression leftOperand, Span span, Expression rightOperand) {
		super(leftOperand, span, rightOperand);
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		Label end = new Label();
		compiler.visit(getLeftOperand())
				.insn(DUP)
				.invoke(INVOKESTATIC, OperatorHandle.class, "isTrue", boolean.class, Object.class)
				.jump(IFEQ, end)
				.insn(POP)
				.visit(getRightOperand())
				.label(end);
	}
}
