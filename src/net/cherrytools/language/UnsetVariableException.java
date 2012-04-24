package net.cherrytools.language;

public class UnsetVariableException extends ZemException {
    private static final long serialVersionUID = -3917677724213982759L;

    public UnsetVariableException(String variableName, SourcePosition pos) {
        super(variableName + " is not set", pos);
    }
}
