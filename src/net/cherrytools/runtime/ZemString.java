package net.cherrytools.runtime;

final public class ZemString extends ZemObject {
    private String value;

    public ZemString(String value) {
        this.value = value;
    }

    public ZemString concat(ZemString other) {
        return new ZemString(value + other.value);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int compareTo(ZemObject object) {
        ZemString str = (ZemString) object;
        return value.compareTo(str.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        return compareTo((ZemObject) object) == 0;
    }
}
