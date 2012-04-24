package net.cherrytools.language.ast;

import java.util.Map;

import net.cherrytools.language.Interpreter;
import net.cherrytools.language.InvalidTypeException;
import net.cherrytools.language.SourcePosition;
import net.cherrytools.runtime.Dictionary;
import net.cherrytools.runtime.ZemArray;
import net.cherrytools.runtime.ZemObject;

public class LookupNode extends Node {
    private VariableNode varNode;
    private Node keyNode;

    public LookupNode(SourcePosition pos, VariableNode varNode, Node keyNode) {
        super(pos);
        this.varNode = varNode;
        this.keyNode = keyNode;
    }

    public ZemObject get(Interpreter interpreter) {
        ZemObject var = interpreter.getVariable(varNode.getName(), varNode.getPosition());
        ZemObject ret = null;
        if (var instanceof ZemArray) {
            int index = keyNode.eval(interpreter).toNumber(keyNode.getPosition()).intValue();
            return ((ZemArray) var).get(index);
        } else if (var instanceof Dictionary) {
            ZemObject key = keyNode.eval(interpreter);
            return ((Dictionary) var).get(key);
        }
        throw new InvalidTypeException("lookup expects an array or dictionary.", getPosition());
    }

    public void set(Interpreter interpreter, ZemObject result) {
        ZemObject var = interpreter.getVariable(varNode.getName(), varNode.getPosition());
        ZemObject ret = null;
        if (var instanceof ZemArray) {
            int index = keyNode.eval(interpreter).toNumber(keyNode.getPosition()).intValue();
            ((ZemArray) var).set(index, result);
            return;
        } else if (var instanceof Dictionary) {
            ZemObject key = keyNode.eval(interpreter);
            ((Dictionary) var).set(key, result);
            return;
        }
        throw new InvalidTypeException("lookup expects an array or dictionary.", getPosition());
    }

    @Override
    public ZemObject eval(Interpreter interpreter) {
        return get(interpreter);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append('(');
        sb.append("lookup ");
        sb.append(varNode);
        sb.append(' ');
        sb.append(keyNode);
        sb.append(')');
        return sb.toString();
    }
}
