package net.cherrytools.runtime;

public class DictionaryEntry extends ZemObject {
    private ZemObject key;
    private ZemObject value;

    public DictionaryEntry(ZemObject key, ZemObject value) {
        this.key = key;
        this.value = value;
    }

    public ZemObject getKey() {
        return key;
    }

    public ZemObject getValue() {
        return value;
    }

    public void setValue(ZemObject value) {
        this.value = value;
    }

    @Override
    public int compareTo(ZemObject o) {
        DictionaryEntry entry = (DictionaryEntry) o;
        return value.compareTo(entry.value);
    }

    @Override
    public String toString() {
        return key.toString() + "=" + value.toString();
    }
}
