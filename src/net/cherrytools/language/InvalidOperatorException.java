package net.cherrytools.language;

public class InvalidOperatorException extends ZemException {
    private static final long serialVersionUID = -57261291654807212L;

    public InvalidOperatorException(SourcePosition pos) {
        super("Invalid operator", pos);
    }
}
