package net.cherrytools.language;

public class InvalidTypeException extends ZemException {
    private static final long serialVersionUID = 9115378805326306069L;

    public InvalidTypeException(String message, SourcePosition position) {
        super(message, position);
    }
}
