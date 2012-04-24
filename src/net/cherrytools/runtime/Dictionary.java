package net.cherrytools.runtime;

import java.util.Iterator;
import java.util.Map;

public class Dictionary extends ZemObject implements Iterable<Map.Entry<ZemObject, ZemObject>> {
    private Map<ZemObject, ZemObject> dict;

    public Dictionary(Map<ZemObject, ZemObject> dict) {
        this.dict = dict;
    }

    public ZemObject get(ZemObject key) {
        return dict.get(key);
    }

    public void set(ZemObject key, ZemObject value) {
        dict.put(key, value);
    }

    @Override
    public int compareTo(ZemObject o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return dict.toString();
    }

    @Override
    public Iterator<Map.Entry<ZemObject, ZemObject>> iterator() {
        return dict.entrySet().iterator();
    }
}
