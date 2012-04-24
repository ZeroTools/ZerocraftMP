package net.cherrytools.runtime;

import java.util.Iterator;
import java.util.List;

public class ZemArray extends ZemObject implements Iterable<ZemObject> {
    private List<ZemObject> elements;

    public ZemArray(List<ZemObject> elements) {
        this.elements = elements;
    }

    public ZemObject get(int index) {
        return elements.get(index);
    }

    public void set(int index, ZemObject element) {
        elements.set(index, element);
    }

    public int size() {
        return elements.size();
    }

    public void push(ZemObject element) {
        elements.add(element);
    }

    @Override
    public Iterator<ZemObject> iterator() {
        return elements.iterator();
    }

    @Override
    public int compareTo(ZemObject o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return elements.toString();
    }
}
