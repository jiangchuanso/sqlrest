package com.gitee.sqlrest.script.parsing.ast.linq;

import com.gitee.sqlrest.script.parsing.Span;
import com.gitee.sqlrest.script.parsing.VarIndex;
import com.gitee.sqlrest.script.parsing.ast.Expression;
import com.gitee.sqlrest.script.parsing.ast.VariableSetter;
import com.gitee.sqlrest.script.parsing.ast.statement.MemberAccess;

public class LinqField extends LinqExpression implements VariableSetter {

	private final String aliasName;

	private final VarIndex varIndex;

	public LinqField(Span span, Expression expression, VarIndex alias) {
		super(span, expression);
		if (expression instanceof MemberAccess){
			this.aliasName = alias != null ? alias.getName() : ((MemberAccess)expression).getName().getText();
		} else {
			this.aliasName = alias != null ? alias.getName() : expression.getSpan().getText();
		}
		this.varIndex = alias;
	}

	public VarIndex getVarIndex() {
		return varIndex;
	}

	public String getAlias() {
		return aliasName;
	}

}
