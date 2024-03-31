package com.gitee.sqlrest.script.parsing.ast.literal;

import com.gitee.sqlrest.script.parsing.Span;
import com.gitee.sqlrest.script.parsing.ast.Literal;

public class NumberLiteral extends Literal {

	protected boolean neg;

	public NumberLiteral(Span span) {
		super(span);
	}

	public NumberLiteral(Span span, Object value) {
		super(span, value);
	}

	public String getText() {
		return (neg ? "-" : "") + getSpan().getText();
	}

	public void setNeg(boolean neg) {
		this.neg = neg;
	}
}
