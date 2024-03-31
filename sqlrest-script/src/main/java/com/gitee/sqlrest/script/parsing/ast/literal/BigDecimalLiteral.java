package com.gitee.sqlrest.script.parsing.ast.literal;

import com.gitee.sqlrest.script.compile.MagicScriptCompiler;
import com.gitee.sqlrest.script.parsing.Span;

import java.math.BigDecimal;

/**
 * int常量
 */
public class BigDecimalLiteral extends NumberLiteral {
	public BigDecimalLiteral(Span literal) {
		super(literal);
	}

	@Override
	public void compile(MagicScriptCompiler compiler) {
		String text = getText();
		compiler.typeInsn(NEW, BigDecimal.class)
				.insn(DUP)
				.ldc(text.substring(0, text.length() - 1).replace("_", ""))
				.invoke(INVOKESPECIAL, BigDecimal.class, "<init>", void.class, String.class);
	}
}
