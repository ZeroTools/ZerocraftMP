package net.cherrytools.language.ast;

import net.cherrytools.language.Interpreter;
import net.cherrytools.language.SourcePosition;
import net.cherrytools.runtime.ZemNumber;
import net.cherrytools.runtime.ZemObject;

public class NegateOpNode extends UnaryOpNode implements IArithmeticOpNode {
    public NegateOpNode(SourcePosition pos, Node value) {
        super(pos, "-", value);
    }

    @Override
    public ZemObject eval(Interpreter interpreter) {
        ZemNumber operand = getOperand().eval(interpreter).toNumber(getOperand().getPosition());
        return operand.negate();
    }
}
