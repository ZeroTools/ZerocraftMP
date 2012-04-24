package net.cherrytools.language.ast;

import net.cherrytools.language.Interpreter;
import net.cherrytools.language.SourcePosition;
import net.cherrytools.runtime.ZemBoolean;
import net.cherrytools.runtime.ZemObject;

public class NotEqualsOpNode extends RelationalOpNode {
    public NotEqualsOpNode(SourcePosition pos, Node left, Node right) {
        super(pos, "!=", left, right);
    }

    @Override
    public ZemObject eval(Interpreter interpreter) {
        return equals(interpreter).not();
    }
}
