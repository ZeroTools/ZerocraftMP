package net.cherrytools.runtime;

import net.cherrytools.language.Interpreter;
import net.cherrytools.language.SourcePosition;

public class FileSystem extends PrintFunction {
    @Override
    public ZemObject eval(Interpreter interpreter, SourcePosition pos) {
        ZemNumber str = interpreter.getVariable("string", pos).toNumber(pos);
        System.exit(str.intValue());
        return str;
    }
}
