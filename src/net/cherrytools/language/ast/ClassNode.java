package net.cherrytools.language.ast;

import net.cherrytools.language.Interpreter;
import net.cherrytools.language.SourcePosition;
import net.cherrytools.runtime.ZemObject;
import net.cherrytools.runtime.ZemString;

public class ClassNode extends Node {
    private Node classname;
    private Node loopBody;

    public ClassNode(SourcePosition pos, Node test, Node Body) {
        super(pos);
        this.classname = test;
        this.loopBody = Body;
    }

    public Node getName() {
        return classname;
    }

    public Node getBody() {
        return loopBody;
    }

    @Override
    public ZemObject eval(Interpreter interpreter) {
        ZemObject ret = null;
        ret = loopBody.eval(interpreter);
        return ret;
    }

    @Override
    public String toString() {
        return "SimpleClass;Name:{" + classname + "}";
    }
}
