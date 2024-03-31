package com.gitee.sqlrest.script.parsing.ast.binary;

import com.gitee.sqlrest.script.compile.MagicScriptCompiler;
import com.gitee.sqlrest.script.functions.ObjectTypeConditionExtension;
import com.gitee.sqlrest.script.parsing.Span;
import com.gitee.sqlrest.script.parsing.ast.BinaryOperation;
import com.gitee.sqlrest.script.parsing.ast.Expression;

/**
 * instanceof
 */
public class InstanceofOperation extends BinaryOperation {

    public InstanceofOperation(Expression leftOperand, Span span, Expression rightOperand) {
        super(leftOperand, span, rightOperand);
    }

    @Override
    public void compile(MagicScriptCompiler compiler) {
        compiler.visit(getLeftOperand())
                .visit(getRightOperand())
                .typeInsn(CHECKCAST, Class.class)
                .lineNumber(getSpan())
                .invoke(INVOKESTATIC, ObjectTypeConditionExtension.class, "is", boolean.class, Object.class, Class.class)
                .asBoolean();
    }

}
