package net.cherrytools.runtime;

public class ReturnException extends RuntimeException {
    private static final long serialVersionUID = -667377947471909097L;

    private ZemObject ret;

    public ReturnException(ZemObject ret) {
        this.ret = ret;
    }

    public ZemObject getReturn() {
        return ret;
    }

    /**
     * This method doesn't do anything for performance reasons.
     *
     * @see Throwable#fillInStackTrace()
     */
    public Throwable fillInStackTrace() {
        return this;
    }
}
