package com.gitee.sqlrest.script.parsing.ast.linq;

import com.gitee.sqlrest.script.parsing.Span;
import com.gitee.sqlrest.script.parsing.VarIndex;
import com.gitee.sqlrest.script.parsing.ast.Expression;

public class LinqOrder extends LinqField {

	/**
	 * 1 正序
	 * -1 倒序
	 */
	private final int order;

	public LinqOrder(Span span, Expression expression, VarIndex alias, int order) {
		super(span, expression, alias);
		this.order = order;
	}

	public int getOrder() {
		return order;
	}
}
