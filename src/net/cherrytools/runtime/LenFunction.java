package net.cherrytools.runtime;

import net.cherrytools.language.Interpreter;
import net.cherrytools.language.SourcePosition;

public class LenFunction extends Function {
    @Override
    public ZemObject getDefaultValue(int index) {
        return null;
    }

    @Override
    public int getParameterCount() {
        return 1;
    }

    @Override
    public String getParameterName(int index) {
        return "string";
    }

    @Override
    public ZemObject eval(Interpreter interpreter, SourcePosition pos) {
        ZemString str = interpreter.getVariable("string", pos).toZString();
        return new ZemNumber(str.toString().length());
    }
}
