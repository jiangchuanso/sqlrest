package com.gitee.sqlrest.script.parsing.ast.binary;

import com.gitee.sqlrest.script.MagicScriptError;
import com.gitee.sqlrest.script.compile.MagicScriptCompiler;
import com.gitee.sqlrest.script.parsing.Span;
import com.gitee.sqlrest.script.parsing.ast.BinaryOperation;
import com.gitee.sqlrest.script.parsing.ast.Expression;
import com.gitee.sqlrest.script.parsing.ast.VariableSetter;
import com.gitee.sqlrest.script.parsing.ast.statement.VariableAccess;

/**
 * = 操作
 */
public class AssigmentOperation extends BinaryOperation {

	public AssigmentOperation(Expression leftOperand, Span span, Expression rightOperand) {
		super(leftOperand, span, rightOperand);
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		if (getLeftOperand() instanceof VariableAccess) {
			compiler.pre_store(((VariableAccess) getLeftOperand()).getVarIndex())
					.compile(getRightOperand());
			if (getRightOperand() instanceof AssigmentOperation) {
				compiler.visit(((AssigmentOperation) getRightOperand()).getLeftOperand());
			}
			compiler.store(((VariableAccess) getLeftOperand()).getVarIndex());
		} else if (getLeftOperand() instanceof VariableSetter) {
			compiler.newRuntimeContext();
			((VariableSetter) getLeftOperand()).compile_visit_variable(compiler);
			compiler.compile(getRightOperand()).call("set_variable_value", 4);
		} else {
			MagicScriptError.error("赋值目标应为变量", getLeftOperand().getSpan());
		}
	}
}
