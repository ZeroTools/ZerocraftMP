package net.cherrytools.runtime;

import net.cherrytools.language.Interpreter;
import net.cherrytools.language.SourcePosition;

public class PrintLineFunction extends PrintFunction {
    @Override
    public ZemObject eval(Interpreter interpreter, SourcePosition pos) {
        ZemString str = interpreter.getVariable("string", pos).toZString();
        System.out.println("[Gamemode]: " + str.toString());
        return str;
    }
}
