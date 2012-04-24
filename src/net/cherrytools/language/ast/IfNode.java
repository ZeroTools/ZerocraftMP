package net.cherrytools.language.ast;

import net.cherrytools.language.Interpreter;
import net.cherrytools.language.SourcePosition;
import net.cherrytools.runtime.ZemBoolean;
import net.cherrytools.runtime.ZemObject;

public class IfNode extends Node {
    private Node testCondition;
    private Node thenBlock;
    private Node elseBlock;

    public IfNode(SourcePosition pos, Node testCondition, Node thenBlock, Node elseBlock) {
        super(pos);
        this.testCondition = testCondition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }

    public Node getTestCondition() {
        return testCondition;
    }

    public Node getThenBlock() {
        return thenBlock;
    }

    public Node getElseBlock() {
        return elseBlock;
    }

    @Override
    public ZemObject eval(Interpreter interpreter) {
        boolean test = testCondition.eval(interpreter).toBoolean(testCondition.getPosition()).booleanValue();
        if (test) {
            return thenBlock.eval(interpreter);
        } else if (elseBlock != null) {
            return elseBlock.eval(interpreter);
        }
        return ZemBoolean.FALSE;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append("when ");
        sb.append(testCondition);
        sb.append(' ');
        sb.append(thenBlock);
        if (elseBlock != null) {
            sb.append(' ');
            sb.append(elseBlock);
        }
        sb.append(')');
        return sb.toString();
    }
}
