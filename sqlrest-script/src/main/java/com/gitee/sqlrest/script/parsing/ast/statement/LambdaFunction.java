package com.gitee.sqlrest.script.parsing.ast.statement;

import com.gitee.sqlrest.script.compile.MagicScriptCompiler;
import com.gitee.sqlrest.script.parsing.Span;
import com.gitee.sqlrest.script.parsing.VarIndex;
import com.gitee.sqlrest.script.parsing.ast.Expression;
import com.gitee.sqlrest.script.parsing.ast.Node;

import java.util.List;

public class LambdaFunction extends Expression {
	private final List<VarIndex> parameters;
	private final List<Node> childNodes;
	private String methodName;

	private boolean async;

	public LambdaFunction(Span span, List<VarIndex> parameters, List<Node> childNodes) {
		super(span);
		this.parameters = parameters;
		this.childNodes = childNodes;
	}

	@Override
	public void visitMethod(MagicScriptCompiler compiler) {
		this.methodName = compiler.visitMethod((async ? "async_" : "") + "lambda", childNodes, parameters);
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	public List<VarIndex> getParameters() {
		return parameters;
	}

	/**
	 * 访问lambda方法
	 */
	private void compileMethod(MagicScriptCompiler compiler) {
		compiler.load0()
				.lambda(methodName);
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		compileMethod(compiler);
	}
}
