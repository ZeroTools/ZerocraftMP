package net.cherrytools.language.ast;

import net.cherrytools.language.Interpreter;
import net.cherrytools.language.SourcePosition;
import net.cherrytools.runtime.ReturnException;
import net.cherrytools.runtime.ZemObject;

public class ReturnNode extends Node {
    private Node expression;

    public ReturnNode(SourcePosition pos, Node expression) {
        super(pos);
        this.expression = expression;
    }

    @Override
    public ZemObject eval(Interpreter interpreter) {
        throw new ReturnException(expression.eval(interpreter));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(return ");
        sb.append(expression);
        sb.append(')');
        return sb.toString();
    }
}
