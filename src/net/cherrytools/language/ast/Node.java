package net.cherrytools.language.ast;

import net.cherrytools.language.SourcePosition;
import net.cherrytools.language.Interpreter;
import net.cherrytools.runtime.ZemObject;

public abstract class Node {
    private SourcePosition position;

    public Node(SourcePosition position) {
        this.position = position;
    }

    public SourcePosition getPosition() {
        return position;
    }

    abstract public ZemObject eval(Interpreter interpreter);
}
