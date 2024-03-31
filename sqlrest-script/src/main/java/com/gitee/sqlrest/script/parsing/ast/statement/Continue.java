package com.gitee.sqlrest.script.parsing.ast.statement;

import com.gitee.sqlrest.script.compile.MagicScriptCompiler;
import com.gitee.sqlrest.script.parsing.Span;
import com.gitee.sqlrest.script.parsing.ast.Node;

/**
 * continue语句
 */
public class Continue extends Node {

	public Continue(Span span) {
		super(span);
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		compiler.start();
	}
}