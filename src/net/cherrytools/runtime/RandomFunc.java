package net.cherrytools.runtime;

import net.cherrytools.language.Interpreter;
import net.cherrytools.language.SourcePosition;

/**
 * @author Buddy251
 */
public class RandomFunc extends Function {
    @Override
    public ZemObject eval(Interpreter interpreter, SourcePosition pos) {
        ZemNumber str = interpreter.getVariable("low", pos).toNumber(pos);
        ZemNumber bbt = interpreter.getVariable("high", pos).toNumber(pos);
    	int c = (int) (Math.random() * (bbt.intValue() - str.intValue()) + str.intValue());  
        return new ZemNumber(c);
    }

    @Override
    public int getParameterCount() {
        return 2;
    }

    @Override
    public String getParameterName(int index) {
        switch(index) {
            case 1: { return "low"; }
            case 2: { return "high"; }
        }
        return null;
    }

    @Override
    public ZemObject getDefaultValue(int index) {
        switch(index) {
            case 1: { return new ZemNumber(0); }
            case 2: { return new ZemNumber(0); }
        }
        return null;
    }
}
