package com.gitee.sqlrest.script.parsing.ast.statement;

import com.gitee.sqlrest.script.compile.MagicScriptCompiler;
import com.gitee.sqlrest.script.parsing.Span;
import com.gitee.sqlrest.script.parsing.ast.Expression;
import com.gitee.sqlrest.script.parsing.ast.VariableSetter;

public class MapOrArrayAccess extends Expression implements VariableSetter {
	private final Expression mapOrArray;
	private final Expression keyOrIndex;

	public MapOrArrayAccess(Span span, Expression mapOrArray, Expression keyOrIndex) {
		super(span);
		this.mapOrArray = mapOrArray;
		this.keyOrIndex = keyOrIndex;
	}

	@Override
	public void visitMethod(MagicScriptCompiler compiler) {
		mapOrArray.visitMethod(compiler);
		keyOrIndex.visitMethod(compiler);
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		compiler.visit(mapOrArray)
				.visit(keyOrIndex)
				.operator("map_or_array_access");
	}

	@Override
	public void compile_visit_variable(MagicScriptCompiler compiler) {
		compiler.visit(mapOrArray).visit(keyOrIndex);
	}
}