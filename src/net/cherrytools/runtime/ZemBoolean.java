package net.cherrytools.runtime;

final public class ZemBoolean extends ZemObject {
    static final public ZemBoolean TRUE = new ZemBoolean(true);
    static final public ZemBoolean FALSE = new ZemBoolean(false);

    private boolean value;

    private ZemBoolean(boolean value) {
        this.value = value;
    }

    public boolean booleanValue() {
        return this.value;
    }

    static public ZemBoolean valueOf(boolean value) {
        return value ? TRUE : FALSE;
    }

    public ZemBoolean and(ZemBoolean bool) {
        return valueOf(this.value && bool.value);
    }

    public ZemBoolean or(ZemBoolean bool) {
        return valueOf(this.value || bool.value);
    }

    public ZemBoolean not() {
        return valueOf(!this.value);
    }

    @Override
    public ZemString toZString() {
        return new ZemString(this.toString());
    }

    @Override
    public String toString() {
        return this == TRUE ? "true" : "false";
    }

    @Override
    public int compareTo(ZemObject o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object object) {
        return this == object;
    }
}
