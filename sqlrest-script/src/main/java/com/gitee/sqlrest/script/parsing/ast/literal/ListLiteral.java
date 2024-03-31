package com.gitee.sqlrest.script.parsing.ast.literal;

import com.gitee.sqlrest.script.asm.Opcodes;
import com.gitee.sqlrest.script.compile.MagicScriptCompiler;
import com.gitee.sqlrest.script.parsing.Span;
import com.gitee.sqlrest.script.parsing.ast.Expression;
import com.gitee.sqlrest.script.parsing.ast.Literal;
import com.gitee.sqlrest.script.parsing.ast.statement.Spread;

import java.util.List;

/**
 * List常量
 */
public class ListLiteral extends Literal {

	public final List<Expression> values;

	public ListLiteral(Span span, List<Expression> values) {
		super(span);
		this.values = values;
	}

	@Override
	public void visitMethod(MagicScriptCompiler compiler) {
		values.forEach(expr -> expr.visitMethod(compiler));
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		int size = values.size();
		if (size == 0) {
			compiler.newArrayList();
		} else {
			compiler.insn(values.stream().anyMatch(it -> it instanceof Spread) ? Opcodes.ICONST_1 : Opcodes.ICONST_0)
					.asBoolean()
					.newArray(values)
					.call("newArrayList", 2);
		}
	}
}