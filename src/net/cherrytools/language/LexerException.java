package net.cherrytools.language;

public class LexerException extends ZemException {
    private static final long serialVersionUID = -6905527358249165699L;

    public LexerException(String message, int lineNo, int column) {
        super(message, new SourcePosition(lineNo, column));
    }
}
