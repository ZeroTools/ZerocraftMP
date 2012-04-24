package net.cherrytools.runtime;

import java.io.File;
import java.io.IOException;

import net.cherrytools.language.Interpreter;
import net.cherrytools.language.SourcePosition;

public class FileExist extends Function {
    @Override
    public ZemObject eval(Interpreter interpreter, SourcePosition pos) {
        ZemBoolean str = interpreter.getVariable("file", pos).toBoolean(pos);
        File file = new File(str.toString());
        if(!file.exists()) {
            return str.FALSE;
        } else {
            return str.TRUE;
        }
    }

    @Override
    public int getParameterCount() {
        return 2;
    }

    @Override
    public String getParameterName(int index) {
        switch(index) {
            case 1: { return "file"; }
        }
        return null;
    }

    @Override
    public ZemObject getDefaultValue(int index) {
        return null;
    }
}
