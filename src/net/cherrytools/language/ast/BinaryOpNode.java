package net.cherrytools.language.ast;

import net.cherrytools.language.SourcePosition;
public abstract class BinaryOpNode extends Node {
    protected String operator;
    protected Node left;
    protected Node right;
    protected BinaryOpNode(SourcePosition pos, String operator, Node left, Node right) {
        super(pos);
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    /**
     * Return operator symbol
     */
    public String getName() {
        return operator;
    }

    /**
     * Get left operand
     */
    public Node getLeft() {
        return left;
    }

    /**
     * Get right operand
     */
    public Node getRight() {
        return right;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append(getName());
        sb.append(' ');
        sb.append(left.toString());
        sb.append(' ');
        sb.append(right.toString());
        sb.append(')');
        return sb.toString();
    }
}
