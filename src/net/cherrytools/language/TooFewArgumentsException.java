package net.cherrytools.language;

public class TooFewArgumentsException extends ZemException {
    private static final long serialVersionUID = -8841576834370732148L;

    public TooFewArgumentsException(
            String functionName,
            int noArgsRequired,
            int noArgs,
            SourcePosition pos) {
        super(functionName + " expects at least " + noArgsRequired
                + " arguments but got " + noArgs, pos);
    }
}
