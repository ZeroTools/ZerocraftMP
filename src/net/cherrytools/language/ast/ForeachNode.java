package net.cherrytools.language.ast;

import java.util.Map;

import net.cherrytools.language.Interpreter;
import net.cherrytools.language.SourcePosition;
import net.cherrytools.language.InvalidTypeException;
import net.cherrytools.runtime.Dictionary;
import net.cherrytools.runtime.ZemArray;
import net.cherrytools.runtime.ZemObject;

public class ForeachNode extends Node {
    private VariableNode onVariableNode;
    private Node asNode;
    private Node loopBody;

    public ForeachNode(SourcePosition pos, VariableNode onVariableNode, Node asNode, Node loopBody) {
        super(pos);
        this.onVariableNode = onVariableNode;
        this.asNode = asNode;
        this.loopBody = loopBody;
    }

    @Override
    public ZemObject eval(Interpreter interpreter) {
        ZemObject onVariable = interpreter.getVariable(onVariableNode.getName(), onVariableNode.getPosition());
        ZemObject ret = null;
        if (onVariable instanceof ZemArray) {
            String asVariableName = asNode.toString();
            for (ZemObject element : (ZemArray) onVariable) {
                interpreter.setVariable(asVariableName, element);
                ret = loopBody.eval(interpreter);
            }
            return ret;
        } else if (onVariable instanceof Dictionary) {
        	DictionaryEntryNode entryNode = null;
        	try {
            entryNode = (DictionaryEntryNode)asNode;
        	} catch(ClassCastException e) {
        		return null;
        	}
            String keyName = ((VariableNode) entryNode.getKey()).getName();
            String valueName = ((VariableNode) entryNode.getValue()).getName();
            for (Map.Entry<ZemObject, ZemObject> entry : (Dictionary) onVariable) {
                interpreter.setVariable(keyName, entry.getKey());
                interpreter.setVariable(valueName, entry.getValue());
                ret = loopBody.eval(interpreter);
            }
            return ret;
        }
        throw new InvalidTypeException("foreach expects an array or dictionary.", onVariableNode.getPosition());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append("foreach ");
        sb.append(onVariableNode);
        sb.append(' ');
        sb.append(asNode);
        sb.append(' ');
        sb.append(loopBody);
        sb.append(')');
        return sb.toString();
    }
}
