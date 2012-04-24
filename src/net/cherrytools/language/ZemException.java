package net.cherrytools.language;

public class ZemException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private ZemException() {
    }

    public ZemException(String message) {
        super(message);
    }

    public ZemException(String message, SourcePosition position) {
        super(message + " on line " + position.getLineNumber() +
            " at column " + position.getColumnNumber());
    }
}
