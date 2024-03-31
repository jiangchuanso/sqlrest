package com.gitee.sqlrest.script.parsing.ast.statement;

import com.gitee.sqlrest.script.asm.Opcodes;
import com.gitee.sqlrest.script.compile.MagicScriptCompiler;
import com.gitee.sqlrest.script.parsing.Span;
import com.gitee.sqlrest.script.parsing.ast.Expression;
import com.gitee.sqlrest.script.runtime.SpreadValue;

/**
 * 展开语法 Spread syntax (...)
 */
public class Spread extends Expression {


	private final Expression target;

	public Spread(Span span, Expression target) {
		super(span);
		this.target = target;
	}

	@Override
	public void visitMethod(MagicScriptCompiler compiler) {
		target.visitMethod(compiler);
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		// 对于...xxx 的参数 统一转换为 new SpreadValue(object)
		compiler.typeInsn(Opcodes.NEW, SpreadValue.class)
				.insn(Opcodes.DUP)
				.visit(target)
				.invoke(Opcodes.INVOKESPECIAL, SpreadValue.class, "<init>", void.class, Object.class);
	}
}
