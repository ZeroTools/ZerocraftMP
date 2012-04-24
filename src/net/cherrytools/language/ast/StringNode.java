package net.cherrytools.language.ast;

import net.cherrytools.language.Interpreter;
import net.cherrytools.language.SourcePosition;
import net.cherrytools.runtime.ZemObject;
import net.cherrytools.runtime.ZemString;

public class StringNode extends Node {
    private ZemString literal;

    public StringNode(SourcePosition pos, String literal) {
        super(pos);
        this.literal = new ZemString(literal);
    }

    @Override
    public ZemObject eval(Interpreter interpreter) {
        return literal;
    }

    @Override
    public String toString() {
        return '"' + literal.toString() + '"';
    }
}
