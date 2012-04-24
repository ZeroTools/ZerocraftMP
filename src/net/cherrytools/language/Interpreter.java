package net.cherrytools.language;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.cherrytools.language.ast.RootNode;
import net.cherrytools.runtime.ArrayPushFunction;
import net.cherrytools.runtime.CloseFunction;
import net.cherrytools.runtime.FileCreate;
import net.cherrytools.runtime.FileExist;
import net.cherrytools.runtime.FileSystem;
import net.cherrytools.runtime.Function;
import net.cherrytools.runtime.LenFunction;
import net.cherrytools.runtime.PrintFunction;
import net.cherrytools.runtime.PrintLineFunction;
import net.cherrytools.runtime.RandomFunc;
import net.cherrytools.runtime.ZemObject;

public class Interpreter {
    private Map<String, ZemObject> symbolTable = new HashMap<String, ZemObject>();
    public Interpreter() {
        symbolTable.put("write", new PrintLineFunction());
        symbolTable.put("len", new LenFunction());
        symbolTable.put("exit", new CloseFunction());
        symbolTable.put("array_push", new ArrayPushFunction());
        symbolTable.put("random", new RandomFunc());
    }
    public ZemObject getVariable(String name, SourcePosition pos) {
        if (!symbolTable.containsKey(name)) {
            throw new UnsetVariableException(name, pos);
        }
        return symbolTable.get(name);
    }
    public void setVariable(String name, ZemObject value) {
        symbolTable.put(name, value);
    }
    public void checkFunctionExists(String functionName, SourcePosition pos) {
        ZemObject symbol = getVariable(functionName, pos);
        if (!(symbol instanceof Function)) {
            throw new InvalidTypeException(functionName + " is not a function", pos);
        }
    }
    public ZemObject callFunction(String functionName, List<ZemObject> args, SourcePosition pos) {
        Function function = (Function)symbolTable.get(functionName);
        Map<String, ZemObject> savedSymbolTable =
            new HashMap<String, ZemObject>(symbolTable);
        int noMissingArgs = 0;
        int noRequiredArgs = 0;
        for (int paramIndex = 0;
                paramIndex < function.getParameterCount(); paramIndex++) {
            String parameterName = function.getParameterName(paramIndex);
            ZemObject value = function.getDefaultValue(paramIndex);
            if (value == null) {
                noRequiredArgs++;
            }
            if (paramIndex < args.size()) {
                value = args.get(paramIndex);
            }
            if (value == null) {
                noMissingArgs++;
            }
            setVariable(parameterName, value);
        }
        if (noMissingArgs > 0) {
            throw new TooFewArgumentsException(functionName, noRequiredArgs,
                    args.size(), pos);
        }
        ZemObject ret = function.eval(this, pos);
        symbolTable = savedSymbolTable;

        return ret;
    }
    public ZemObject eval(String script) throws IOException {
        return eval(new StringReader(script));
    }
    public ZemObject eval(File file) throws IOException {
        return eval(new BufferedReader(new FileReader(file)));
    }
    public ZemObject eval(Reader reader) throws IOException {
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        RootNode program = parser.program();
        return program.eval(this);
    }
}
