package com.gitee.sqlrest.script.parsing.ast;

import com.gitee.sqlrest.script.parsing.Span;

/**
 * 表达式
 */
public abstract class Expression extends Node {
	public Expression(Span span) {
		super(span);
	}

}