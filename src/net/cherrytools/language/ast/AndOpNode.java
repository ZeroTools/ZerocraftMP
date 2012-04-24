package net.cherrytools.language.ast;

import net.cherrytools.language.Interpreter;
import net.cherrytools.language.SourcePosition;
import net.cherrytools.runtime.ZemBoolean;
import net.cherrytools.runtime.ZemObject;

public class AndOpNode extends BinaryOpNode implements IBooleanOpNode {
    public AndOpNode(SourcePosition pos, Node left, Node right) {
        super(pos, "and", left, right);
    }

    @Override
    public ZemObject eval(Interpreter interpreter) {
        ZemBoolean left = getLeft().eval(interpreter).toBoolean(getLeft().getPosition());
        if (!left.booleanValue()) {
            // Short circuit the operator, since false && test == false
            return left;
        }
        ZemBoolean right = getRight().eval(interpreter).toBoolean(getRight().getPosition());
        return left.and(right);
    }
}
