package net.cherrytools.language.ast;

import net.cherrytools.language.Interpreter;
import net.cherrytools.language.SourcePosition;
import net.cherrytools.runtime.ZemObject;
import net.cherrytools.runtime.ZemString;

public class ConcatOpNode extends BinaryOpNode {
    public ConcatOpNode(SourcePosition pos, Node left, Node right) {
        super(pos, "~", left, right);
    }

    @Override
    public ZemObject eval(Interpreter interpreter) {
        ZemString left = getLeft().eval(interpreter).toZString();
        ZemString right = getRight().eval(interpreter).toZString();
        return left.concat(right);
    }
}
