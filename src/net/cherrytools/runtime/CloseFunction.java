package net.cherrytools.runtime;

import net.cherrytools.language.Interpreter;
import net.cherrytools.language.SourcePosition;

/*
 * @author Buddy251
 */
public class CloseFunction extends Function {
    @Override
    public ZemObject eval(Interpreter interpreter, SourcePosition pos) {
        ZemNumber str = interpreter.getVariable("string", pos).toNumber(pos);
        System.exit(str.intValue());
        return str;
    }

    @Override
    public ZemObject getDefaultValue(int index) {
        return new ZemNumber(0);
    }

    @Override
    public int getParameterCount() {
        return 1;
    }

    @Override
    public String getParameterName(int index) {
        return "int";
    }

}
