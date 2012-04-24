package net.cherrytools.language;

public class ParserException extends ZemException {
    private static final long serialVersionUID = 7505060960165209530L;

    public ParserException(String message) {
        super(message);
    }

    public ParserException(String message, SourcePosition position) {
        super(message, position);
    }
}
