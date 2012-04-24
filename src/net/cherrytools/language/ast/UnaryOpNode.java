package net.cherrytools.language.ast;

import net.cherrytools.language.SourcePosition;

public abstract class UnaryOpNode extends Node {
    protected String operator;
    protected Node operand;

    public UnaryOpNode(SourcePosition pos, String operator, Node operand) {
        super(pos);
        this.operator = operator;
        this.operand = operand;
    }

    /**
     * Return operator symbol
     */
    public String getName() {
        return operator;
    }

    /**
     * Return operand
     */
    public Node getOperand() {
        return operand;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append(getName());
        sb.append(' ');
        sb.append(operand.toString());
        sb.append(')');
        return sb.toString();
    }
}
