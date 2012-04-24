package net.cherrytools.language.ast;

import net.cherrytools.language.Interpreter;
import net.cherrytools.language.SourcePosition;
import net.cherrytools.runtime.ZemNumber;
import net.cherrytools.runtime.ZemObject;

public class NumberNode extends Node {
    private ZemNumber number;

    public NumberNode(SourcePosition pos, String number) {
        super(pos);
        this.number = new ZemNumber(number);
    }

    @Override
    public String toString() {
        return number.toString();
    }

    @Override
    public ZemObject eval(Interpreter interpreter) {
        return number;
    }
}
