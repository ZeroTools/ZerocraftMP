package net.cherrytools.runtime;

import java.io.File;
import java.io.IOException;

import net.cherrytools.language.Interpreter;
import net.cherrytools.language.SourcePosition;

public class FileCreate extends Function {
    @Override
    public ZemObject eval(Interpreter interpreter, SourcePosition pos) {
        ZemNumber str = interpreter.getVariable("file", pos).toNumber(pos);
        File file = new File(str.toString());
        if(!file.exists()) {
        	try {
                    file.createNewFile();
		} catch (IOException e) {
                    e.printStackTrace();
		}
        }
        return str;
    }

    @Override
    public int getParameterCount() {
        return 1;
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
