package net.cherrytools.language.ast;

import net.cherrytools.language.Interpreter;
import net.cherrytools.language.SourcePosition;
import net.cherrytools.runtime.ZemNumber;
import net.cherrytools.runtime.ZemObject;

public class PowerOpNode extends BinaryOpNode implements IArithmeticOpNode {
    public PowerOpNode(SourcePosition pos, Node left, Node right) {
        super(pos, "^", left, right);
    }

    @Override
    public ZemObject eval(Interpreter interpreter) {
        ZemNumber left = getLeft().eval(interpreter).toNumber(getLeft().getPosition());
        ZemNumber right = getRight().eval(interpreter).toNumber(getRight().getPosition());
        return left.power(right);
    }
}
