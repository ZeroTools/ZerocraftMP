package net.cherrytools.language.ast;

import java.util.ArrayList;
import java.util.List;

import net.cherrytools.language.Interpreter;
import net.cherrytools.language.SourcePosition;
import net.cherrytools.runtime.ZemObject;

public class FunctionCallNode extends Node {
    final static public List<Node> NO_ARGUMENTS = new ArrayList<Node>(0);

    private String functionName;
    private List<Node> arguments;

    public FunctionCallNode(SourcePosition pos, String functionName, List<Node> arguments) {
        super(pos);
        this.functionName = functionName;
        this.arguments = arguments;
    }

    @Override
    public ZemObject eval(Interpreter interpreter) {
        interpreter.checkFunctionExists(functionName, getPosition());
        // Evaluate the arguments
        List<ZemObject> args = new ArrayList<ZemObject>(arguments.size());
        for (Node node : arguments) {
            args.add(node.eval(interpreter));
        }
        return interpreter.callFunction(functionName, args, getPosition());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append(functionName);
        for (Node arg : arguments) {
            sb.append(' ');
            sb.append(arg);
        }
        sb.append(')');
        return sb.toString();
    }
}
