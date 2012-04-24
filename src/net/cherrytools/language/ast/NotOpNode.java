package net.cherrytools.language.ast;

import net.cherrytools.language.Interpreter;
import net.cherrytools.language.SourcePosition;
import net.cherrytools.runtime.ZemBoolean;
import net.cherrytools.runtime.ZemObject;

public class NotOpNode extends UnaryOpNode implements IBooleanOpNode {
    public NotOpNode(SourcePosition pos, Node operand) {
        super(pos, "not", operand);
    }

    @Override
    public ZemObject eval(Interpreter interpreter) {
        ZemBoolean operand = getOperand().eval(interpreter).toBoolean(getOperand().getPosition());
        return operand.not();
    }
}
