package net.cherrytools.language.ast;

import net.cherrytools.language.Interpreter;
import net.cherrytools.language.SourcePosition;
import net.cherrytools.runtime.ZemBoolean;
import net.cherrytools.runtime.ZemObject;

public class OrOpNode extends BinaryOpNode implements IBooleanOpNode {
    public OrOpNode(SourcePosition pos, Node left, Node right) {
        super(pos, "or", left, right);
    }

    @Override
    public ZemObject eval(Interpreter interpreter) {
        ZemBoolean left = getLeft().eval(interpreter).toBoolean(getLeft().getPosition());
        if (left.booleanValue()) {
            // Short circuit the operator, since true || test == true
            return left;
        }
        ZemBoolean right = getRight().eval(interpreter).toBoolean(getRight().getPosition());
        return left.or(right);
    }
}
