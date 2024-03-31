package com.gitee.sqlrest.script.parsing.ast.statement;

import com.gitee.sqlrest.script.compile.MagicScriptCompiler;
import com.gitee.sqlrest.script.parsing.Span;
import com.gitee.sqlrest.script.parsing.VarIndex;
import com.gitee.sqlrest.script.parsing.ast.Expression;
import com.gitee.sqlrest.script.parsing.ast.VariableSetter;

public class VariableAccess extends Expression implements VariableSetter {

	private final VarIndex varIndex;

	public VariableAccess(Span name, VarIndex varIndex) {
		super(name);
		this.varIndex = varIndex;
	}

	public VarIndex getVarIndex() {
		return varIndex;
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		compiler.load(varIndex);
	}
}